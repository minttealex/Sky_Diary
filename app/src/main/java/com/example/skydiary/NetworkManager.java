package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static final String BASE_URL = "http://10.0.2.2:5072/";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";

    private static NetworkManager instance;
    private final ApiService apiService;
    private final Context context;

    private NetworkManager(Context context) {
        this.context = context.getApplicationContext();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    public void register(String username, String email, String password, final ApiCallback<UserResponse> callback) {
        Log.d(TAG, "Attempting registration for: " + username);
        Log.d(TAG, "Full URL: " + BASE_URL + "api/auth/register");

        UserRegisterRequest request = new UserRegisterRequest(username, email, password);

        apiService.register(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                Log.d(TAG, "Registration response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Registration successful for: " + response.body().getUsername());
                    saveAuthData(response.body());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Registration failed - Code: " + response.code();
                    Log.e(TAG, error);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Registration failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void login(String username, String password, final ApiCallback<UserResponse> callback) {
        Log.d(TAG, "Attempting login for: " + username);
        Log.d(TAG, "Full URL: " + BASE_URL + "api/auth/login");

        UserLoginRequest request = new UserLoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                Log.d(TAG, "Login response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Login successful for: " + response.body().getUsername());
                    saveAuthData(response.body());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Login failed - Code: " + response.code();
                    Log.e(TAG, error);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Login failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void saveAuthData(UserResponse userResponse) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, userResponse.getToken());
        editor.putString(KEY_USER_ID, userResponse.getId());
        editor.apply();
        Log.d(TAG, "Auth data saved for user: " + userResponse.getUsername());
    }

    public String getToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.apply();

        NoteStorage.getInstance(context).clearAllData();
        NoteStorage.getInstance(context).clearTags();

        Log.d(TAG, "User logged out and local data cleared");
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void syncNotes(List<Note> notes, List<String> deletedNoteIds, Date lastSyncAt, final ApiCallback<SyncResult> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        Log.d(TAG, "Attempting to sync " + notes.size() + " notes, " + deletedNoteIds.size() + " deletions");

        SyncRequest request = new SyncRequest(notes, deletedNoteIds, lastSyncAt);

        Call<SyncResult> call = apiService.syncNotes("Bearer " + token, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SyncResult> call, @NonNull Response<SyncResult> response) {
                Log.d(TAG, "Sync response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    SyncResult syncResult = response.body();
                    int notesCount = syncResult.getNotes() != null ? syncResult.getNotes().size() : 0;
                    int deletedCount = syncResult.getDeletedNoteIds() != null ? syncResult.getDeletedNoteIds().size() : 0;
                    Log.d(TAG, "Sync successful, received " + notesCount + " notes, " + deletedCount + " deletions");
                    callback.onSuccess(syncResult);
                } else {
                    String error = "Sync failed - Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SyncResult> call, @NonNull Throwable t) {
                Log.e(TAG, "Sync failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateUser(String username, String email, final ApiCallback<UserResponse> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        UserUpdateRequest request = new UserUpdateRequest(username, email);

        apiService.updateUser("Bearer " + token, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String error = "Update failed - Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Update failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void uploadLocalNotes(SyncRequest request, final ApiCallback<SyncResult> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        Log.d(TAG, "Uploading " + request.getNotes().size() + " notes");

        Call<SyncResult> call = apiService.uploadLocalNotes("Bearer " + token, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SyncResult> call, @NonNull Response<SyncResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String error = "Upload failed - Code: " + response.code();
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SyncResult> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void syncConstellations(List<UserConstellation> constellations, final ApiCallback<ConstellationSyncResult> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        ConstellationSyncRequest request = new ConstellationSyncRequest(constellations);

        apiService.syncConstellations("Bearer " + token, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ConstellationSyncResult> call, @NonNull Response<ConstellationSyncResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String error = "Constellation sync failed - Code: " + response.code();
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConstellationSyncResult> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getNotes(final ApiCallback<List<Note>> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        Log.d(TAG, "Getting notes from server");

        Call<List<Note>> call = apiService.getNotes("Bearer " + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call, @NonNull Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Got " + response.body().size() + " notes from server");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get notes - Code: " + response.code();
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get notes failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getConstellations(final ApiCallback<List<UserConstellation>> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        Log.d(TAG, "Getting constellations from server");

        Call<List<UserConstellation>> call = apiService.getConstellations("Bearer " + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<UserConstellation>> call, @NonNull Response<List<UserConstellation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Got " + response.body().size() + " constellations from server");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to get constellations - Code: " + response.code();
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserConstellation>> call, @NonNull Throwable t) {
                Log.e(TAG, "Get constellations failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}