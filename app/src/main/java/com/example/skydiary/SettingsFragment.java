package com.example.skydiary;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_LANGUAGE = "app_language";

    private NetworkManager networkManager;
    private UserStorage userStorage;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkManager = NetworkManager.getInstance(requireContext());
        userStorage = UserStorage.getInstance(requireContext());

        if (networkManager.isLoggedIn()) {
            currentUser = userStorage.getCurrentUser();
        } else {
            currentUser = null;
        }

        setupThemeSection(view);
        setupLanguageSection(view);
        setupAccountSection(view);
    }

    private void setupThemeSection(View view) {
        Button buttonAutoTheme = view.findViewById(R.id.button_auto_theme);
        Button buttonLight = view.findViewById(R.id.button_light_theme);
        Button buttonDark = view.findViewById(R.id.button_dark_theme);

        updateThemeButtonStates();

        buttonAutoTheme.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            saveThemePreference("auto");
            updateThemeButtonStates();
        });

        buttonLight.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemePreference("light");
            updateThemeButtonStates();
        });

        buttonDark.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemePreference("dark");
            updateThemeButtonStates();
        });
    }

    private void setupLanguageSection(View view) {
        Button buttonEn = view.findViewById(R.id.button_language_en);
        Button buttonRu = view.findViewById(R.id.button_language_ru);
        Button buttonEs = view.findViewById(R.id.button_language_es);

        updateLanguageButtonStates();

        buttonEn.setOnClickListener(v -> changeLanguage("en"));
        buttonRu.setOnClickListener(v -> changeLanguage("ru"));
        buttonEs.setOnClickListener(v -> changeLanguage("es"));
    }

    private void setupAccountSection(View view) {
        TextView tvAccountStatus = view.findViewById(R.id.tv_account_status);
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnCreateAccount = view.findViewById(R.id.btn_create_account);
        Button btnAccountSettings = view.findViewById(R.id.btn_account_settings);
        LinearLayout llNonLoggedInButtons = view.findViewById(R.id.ll_non_logged_in);

        currentUser = userStorage.getCurrentUser();

        // Debug logging
        Log.d("SettingsFragment", "NetworkManager isLoggedIn: " + networkManager.isLoggedIn());
        Log.d("SettingsFragment", "UserStorage currentUser: " + (currentUser != null ? currentUser.getUsername() : "null"));

        if (networkManager.isLoggedIn()) {
            String displayUsername = "Unknown User";
            if (currentUser != null) {
                displayUsername = currentUser.getUsername();
            } else {
                Log.w("SettingsFragment", "UserStorage is null but NetworkManager is logged in");
            }

            tvAccountStatus.setText(R.string.logged_in_as + displayUsername);
            llNonLoggedInButtons.setVisibility(View.GONE);
            btnAccountSettings.setVisibility(View.VISIBLE);

            btnLogin.setText(R.string.log_out);
            btnLogin.setOnClickListener(v -> new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        networkManager.logout();
                        userStorage.logout();
                        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                        refreshFragment();
                    })
                    .setNegativeButton("Cancel", null)
                    .show());
        } else {
            tvAccountStatus.setText(R.string.not_logged_in);
            llNonLoggedInButtons.setVisibility(View.VISIBLE);
            btnAccountSettings.setVisibility(View.GONE);

            btnLogin.setText(R.string.log_in);
            btnLogin.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LogInFragment())
                    .addToBackStack("settings_to_login")
                    .commit());
        }

        btnCreateAccount.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignUpFragment())
                .addToBackStack("settings_to_create_account")
                .commit());

        btnAccountSettings.setOnClickListener(v -> {
            if (networkManager.isLoggedIn()) {
                showAccountSettingsFragment();
            } else {
                Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAccountSettingsFragment() {
        AccountSettingsFragment fragment = new AccountSettingsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("settings_to_account")
                .commit();
    }

    private void refreshFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private void updateThemeButtonStates() {
        View view = getView();
        if (view == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        String currentTheme = prefs.getString(KEY_THEME, "auto");

        Button buttonAutoTheme = view.findViewById(R.id.button_auto_theme);
        Button buttonLight = view.findViewById(R.id.button_light_theme);
        Button buttonDark = view.findViewById(R.id.button_dark_theme);

        buttonAutoTheme.setAlpha(0.7f);
        buttonLight.setAlpha(0.7f);
        buttonDark.setAlpha(0.7f);

        switch (currentTheme) {
            case "auto":
                buttonAutoTheme.setAlpha(1.0f);
                break;
            case "light":
                buttonLight.setAlpha(1.0f);
                break;
            case "dark":
                buttonDark.setAlpha(1.0f);
                break;
        }
    }

    private void updateLanguageButtonStates() {
        View view = getView();
        if (view == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        String currentLanguage = prefs.getString(KEY_LANGUAGE, "auto");

        Button buttonEn = view.findViewById(R.id.button_language_en);
        Button buttonRu = view.findViewById(R.id.button_language_ru);
        Button buttonEs = view.findViewById(R.id.button_language_es);

        buttonEn.setAlpha(0.7f);
        buttonRu.setAlpha(0.7f);
        buttonEs.setAlpha(0.7f);

        switch (currentLanguage) {
            case "auto":
                break;
            case "en":
                buttonEn.setAlpha(1.0f);
                break;
            case "ru":
                buttonRu.setAlpha(1.0f);
                break;
            case "es":
                buttonEs.setAlpha(1.0f);
                break;
        }
    }

    private void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        requireActivity().recreate();
    }

    private void saveThemePreference(String theme) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    private void changeLanguage(String languageCode) {
        String currentLanguage = getSavedLanguage();
        if (!languageCode.equals(currentLanguage)) {
            saveLanguagePreference(languageCode);
            applyLocale(languageCode);
            requireActivity().recreate();
        }
    }

    private void applyLocale(String langCode) {
        Locale locale;
        if ("auto".equals(langCode)) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(langCode);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(
                config,
                requireActivity().getResources().getDisplayMetrics());
    }

    private void saveLanguagePreference(String langCode) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    private String getSavedLanguage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(KEY_LANGUAGE, "auto");
    }
}