package com.example.skydiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class WelcomeFragment extends Fragment {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    public WelcomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnSignUp = view.findViewById(R.id.btn_sign_up);
        MaterialButton btnLogIn = view.findViewById(R.id.btn_log_in);
        TextView tvSkip = view.findViewById(R.id.tv_skip);

        btnSignUp.setOnClickListener(v -> showSignUpFragment());

        btnLogIn.setOnClickListener(v -> showLogInFragment());

        tvSkip.setOnClickListener(v -> {
            markFirstLaunchCompleted();
            showMainFragment();
        });
    }

    private void showSignUpFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignUpFragment())
                .addToBackStack("welcome_to_signup")
                .commit();
    }

    private void showLogInFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LogInFragment())
                .addToBackStack("welcome_to_login")
                .commit();
    }

    private void showMainFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
    }

    private void markFirstLaunchCompleted() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    public static boolean isFirstLaunch(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }
}