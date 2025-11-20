package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;

public class UserStorage {
    private static final String TAG = "UserStorage";
    private static final String PREFS_NAME = "user_storage";
    private static final String KEY_CURRENT_USER = "current_user";

    private static UserStorage instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private UserStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized UserStorage getInstance(Context context) {
        if (instance == null) {
            instance = new UserStorage(context);
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        if (user == null) {
            Log.e(TAG, "Attempting to save null user!");
            return;
        }

        String userJson = gson.toJson(user);
        Log.d(TAG, "Saving user to storage: " + user.getUsername());
        prefs.edit().putString(KEY_CURRENT_USER, userJson).apply();

        String savedJson = prefs.getString(KEY_CURRENT_USER, null);
        Log.d(TAG, "User saved verification: " + (savedJson != null ? "SUCCESS" : "FAILED"));
    }

    public User getCurrentUser() {
        String userJson = prefs.getString(KEY_CURRENT_USER, null);
        if (userJson == null) {
            Log.d(TAG, "No user found in storage");
            return null;
        }

        try {
            User user = gson.fromJson(userJson, User.class);
            Log.d(TAG, "Retrieved user from storage: " + (user != null ? user.getUsername() : "null"));
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user from storage", e);
            return null;
        }
    }

    public void logout() {
        Log.d(TAG, "Logging out user");
        prefs.edit().remove(KEY_CURRENT_USER).apply();
    }
}