package com.example.skydiary;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_LANGUAGE = "app_language";

    private FirebaseAuth auth;
    private ImageButton btnSync;
    private MaterialButton buttonLanguageSelector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseManager.getInstance().getAuth();

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
        buttonLanguageSelector = view.findViewById(R.id.button_language_selector);
        updateLanguageButtonState();

        buttonLanguageSelector.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 0, 0, "English");
            popup.getMenu().add(0, 1, 1, "Русский");
            popup.getMenu().add(0, 2, 2, "Español");
            popup.setOnMenuItemClickListener(item -> {
                String langCode;
                switch (item.getItemId()) {
                    case 0: langCode = "en"; break;
                    case 1: langCode = "ru"; break;
                    case 2: langCode = "es"; break;
                    default: return false;
                }
                changeLanguage(langCode);
                return true;
            });
            popup.show();
        });
    }

    private void setupAccountSection(View view) {
        TextView tvAccountStatus = view.findViewById(R.id.tv_account_status);
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnCreateAccount = view.findViewById(R.id.btn_create_account);
        Button btnAccountSettings = view.findViewById(R.id.btn_account_settings);
        btnSync = view.findViewById(R.id.btn_sync);
        LinearLayout llNonLoggedInButtons = view.findViewById(R.id.ll_non_logged_in);

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            String displayName = currentUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getEmail();
            }
            tvAccountStatus.setText(String.format("%s %s", getString(R.string.logged_in_as), displayName));
            llNonLoggedInButtons.setVisibility(View.GONE);
            btnAccountSettings.setVisibility(View.VISIBLE);
            btnSync.setVisibility(View.VISIBLE);
            btnLogin.setText(R.string.log_out);
            btnLogin.setOnClickListener(v -> showLogoutConfirmation());

            btnSync.setOnClickListener(v -> performSync());
        } else {
            tvAccountStatus.setText(R.string.not_logged_in);
            llNonLoggedInButtons.setVisibility(View.VISIBLE);
            btnAccountSettings.setVisibility(View.GONE);
            btnSync.setVisibility(View.GONE);
            btnLogin.setText(R.string.log_in);
            btnLogin.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new LogInFragment())
                            .addToBackStack("settings_to_login")
                            .commit());
        }

        btnCreateAccount.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SignUpFragment())
                        .addToBackStack("settings_to_create_account")
                        .commit());

        btnAccountSettings.setOnClickListener(v -> {
            if (auth.getCurrentUser() != null && !auth.getCurrentUser().isAnonymous()) {
                showAccountSettingsFragment();
            } else {
                Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSync() {
        if (!isAdded()) return;

        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Syncing…");
        progress.setCancelable(false);
        progress.show();

        SyncManager syncManager = new SyncManager(requireContext());
        syncManager.syncAll(new SyncManager.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                if (!isAdded()) return;
                progress.dismiss();
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progress.dismiss();
                Toast.makeText(requireContext(), "Sync failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        Log.d("SettingsFragment", "performLogout() called");
        NoteStorage storage = NoteStorage.getInstance(requireContext());
        Log.d("SettingsFragment", "Before clear, notes count: " + storage.getAllNotes().size());
        storage.clearAllNotes();
        Log.d("SettingsFragment", "After clearAllNotes, notes count: " + storage.getAllNotes().size());

        ConstellationStorage.getInstance(requireContext()).resetToDefault();

        auth.signOut();
        auth.signInAnonymously();

        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
    }

    private void showAccountSettingsFragment() {
        AccountSettingsFragment fragment = new AccountSettingsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("settings_to_account")
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
            case "auto": buttonAutoTheme.setAlpha(1.0f); break;
            case "light": buttonLight.setAlpha(1.0f); break;
            case "dark": buttonDark.setAlpha(1.0f); break;
        }
    }

    private void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        requireActivity().recreate();
    }

    private void saveThemePreference(String theme) {
        requireActivity().getSharedPreferences(PREFS_NAME, 0).edit().putString(KEY_THEME, theme).apply();
    }

    private void updateLanguageButtonState() {
        if (!isAdded() || buttonLanguageSelector == null) return;
        String currentLang = getSavedLanguage();
        String displayName;
        switch (currentLang) {
            case "en": displayName = "English"; break;
            case "ru": displayName = "Русский"; break;
            case "es": displayName = "Español"; break;
            default: displayName = "Language"; // system default or unknown
        }
        buttonLanguageSelector.setText(displayName);
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
        Locale locale = "auto".equals(langCode) ? Locale.getDefault() : new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());
    }

    private void saveLanguagePreference(String langCode) {
        requireActivity().getSharedPreferences(PREFS_NAME, 0).edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    private String getSavedLanguage() {
        return requireActivity().getSharedPreferences(PREFS_NAME, 0).getString(KEY_LANGUAGE, "auto");
    }
}