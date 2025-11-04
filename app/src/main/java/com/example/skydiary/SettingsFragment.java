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

        Button buttonEn = view.findViewById(R.id.button_language_en);
        Button buttonRu = view.findViewById(R.id.button_language_ru);
        Button buttonEs = view.findViewById(R.id.button_language_es);

        updateLanguageButtonStates();

        buttonEn.setOnClickListener(v -> changeLanguage("en"));
        buttonRu.setOnClickListener(v -> changeLanguage("ru"));
        buttonEs.setOnClickListener(v -> changeLanguage("es"));
    }

    private void updateThemeButtonStates() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        String currentTheme = prefs.getString(KEY_THEME, "auto");

        Button buttonAutoTheme = requireView().findViewById(R.id.button_auto_theme);
        Button buttonLight = requireView().findViewById(R.id.button_light_theme);
        Button buttonDark = requireView().findViewById(R.id.button_dark_theme);

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
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        String currentLanguage = prefs.getString(KEY_LANGUAGE, "auto");

        Button buttonEn = requireView().findViewById(R.id.button_language_en);
        Button buttonRu = requireView().findViewById(R.id.button_language_ru);
        Button buttonEs = requireView().findViewById(R.id.button_language_es);

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