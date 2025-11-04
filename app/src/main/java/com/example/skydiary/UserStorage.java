package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String PREFS_NAME = "users_prefs";
    private static final String USERS_KEY = "users";
    private static final String CURRENT_USER_KEY = "current_user";

    private static UserStorage instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<User> users;

    private UserStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
        loadUsers();
    }

    public static synchronized UserStorage getInstance(Context context) {
        if (instance == null) {
            instance = new UserStorage(context);
        }
        return instance;
    }

    private void loadUsers() {
        String json = prefs.getString(USERS_KEY, "[]");
        Type listType = new TypeToken<List<User>>(){}.getType();
        users = gson.fromJson(json, listType);
        if (users == null) {
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        String json = gson.toJson(users);
        prefs.edit().putString(USERS_KEY, json).apply();
    }

    public boolean registerUser(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty()) return false;

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username) ||
                    (email != null && !email.isEmpty() && user.getEmail() != null && user.getEmail().equalsIgnoreCase(email))) {
                return false;
            }
        }

        User newUser = new User(username.trim(), email != null ? email.trim() : "", password);
        users.add(newUser);
        saveUsers();
        return true;
    }

    public User loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                setCurrentUser(user);
                return user;
            }
        }
        return null;
    }

    public void setCurrentUser(User user) {
        String userJson = gson.toJson(user);
        prefs.edit().putString(CURRENT_USER_KEY, userJson).apply();
    }

    public User getCurrentUser() {
        String userJson = prefs.getString(CURRENT_USER_KEY, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void logout() {
        prefs.edit().remove(CURRENT_USER_KEY).apply();
    }

    public boolean updateUserPassword(String username, String newPassword) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.setPassword(newPassword);
                saveUsers();
                return true;
            }
        }
        return false;
    }

    public boolean deleteUser(String username) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)) {
                users.remove(i);
                saveUsers();

                if (getCurrentUser() != null && getCurrentUser().getUsername().equals(username)) {
                    logout();
                }
                return true;
            }
        }
        return false;
    }
}