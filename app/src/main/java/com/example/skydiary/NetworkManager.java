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

    public String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
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
        Log.d(TAG, "User logged out");
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void syncNotes(List<Note> notes, final ApiCallback<SyncResult> callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not authenticated");
            return;
        }

        Log.d(TAG, "Attempting to sync " + notes.size() + " notes");
        Log.d(TAG, "Full URL: " + BASE_URL + "api/notes/sync");

        apiService.syncNotes("Bearer " + token, notes).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SyncResult> call, @NonNull Response<SyncResult> response) {
                Log.d(TAG, "Sync response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Sync successful, received " + response.body().getNotes().size() + " notes");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Sync failed - Code: " + response.code();
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
            public void onFailure(@NonNull Call<SyncResult> call, @NonNull Throwable t) {
                Log.e(TAG, "Sync failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}