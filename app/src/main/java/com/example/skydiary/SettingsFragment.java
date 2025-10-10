package com.example.skydiary;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_LANGUAGE = "app_language";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonLight = view.findViewById(R.id.button_light_theme);
        Button buttonDark = view.findViewById(R.id.button_dark_theme);

        buttonLight.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemePreference("light");
        });

        buttonDark.setOnClickListener(v -> {
            applyTheme(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemePreference("dark");
        });

        Button buttonEn = view.findViewById(R.id.button_language_en);
        Button buttonRu = view.findViewById(R.id.button_language_ru);

        buttonEn.setOnClickListener(v -> changeLanguage("en"));
        buttonRu.setOnClickListener(v -> changeLanguage("ru"));
    }

    private void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        // Recreate activity to apply theme, or notify activity to recreate
        requireActivity().recreate();
    }

    private void saveThemePreference(String theme) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, 0);
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
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(
                config,
                requireActivity().getResources().getDisplayMetrics());
    }

    private void saveLanguagePreference(String langCode) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    private String getSavedLanguage() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(KEY_LANGUAGE, "en");
    }
}

