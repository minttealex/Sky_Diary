package com.example.skydiary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LogInFragment extends Fragment {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogIn;
    private NetworkManager networkManager;
    private UserStorage userStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkManager = NetworkManager.getInstance(requireContext());
        userStorage = UserStorage.getInstance(requireContext());

        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogIn = view.findViewById(R.id.btn_log_in);

        view.findViewById(R.id.button_back).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnLogIn.setOnClickListener(v -> {
            String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                return;
            }

            btnLogIn.setEnabled(false);
            btnLogIn.setText(R.string.logging_in);

            networkManager.login(username, password, new NetworkManager.ApiCallback<>() {
                @Override
                public void onSuccess(UserResponse result) {
                    User user = new User(result.getUsername(), result.getEmail());
                    userStorage.setCurrentUser(user);

                    User savedUser = userStorage.getCurrentUser();
                    Log.d("LogInFragment", "User saved to storage: " + (savedUser != null ? savedUser.getUsername() : "null"));

                    CloudSyncManager syncManager = new CloudSyncManager(requireContext());
                    syncManager.syncOnLogin(new CloudSyncManager.SyncCallback() {
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            Log.d("Login", "Sync completed: " + message);
                            Toast.makeText(requireContext(), "Login successful! " + message, Toast.LENGTH_SHORT).show();

                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new MainFragment())
                                    .commit();
                        }

                        @Override
                        public void onSyncError(String error) {
                            Log.e("Login", "Sync error: " + error);
                            Toast.makeText(requireContext(), "Login successful but sync failed: " + error, Toast.LENGTH_LONG).show();
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new MainFragment())
                                    .commit();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    btnLogIn.setEnabled(true);
                    btnLogIn.setText(R.string.log_in);
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        TextView tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
                    if (email.isEmpty() || !isValidEmail(email)) {
                        Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        Toast.makeText(requireContext(), "Reset link would be sent to: " + email, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}