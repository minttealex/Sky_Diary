package com.example.skydiary;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class AccountSettingsFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etCurrentPassword, etNewPassword;
    private UserStorage userStorage;
    private NetworkManager networkManager;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userStorage = UserStorage.getInstance(requireContext());
        networkManager = NetworkManager.getInstance(requireContext());
        currentUser = userStorage.getCurrentUser();

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);
        TextView tvDeleteAccount = view.findViewById(R.id.tv_delete_account);

        if (currentUser != null) {
            etUsername.setText(currentUser.getUsername());
            etEmail.setText(currentUser.getEmail());
        }

        buttonBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnUpdate.setOnClickListener(v -> updateAccount());

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        tvDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    private void updateAccount() {
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String currentPassword = Objects.requireNonNull(etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return;
        }

        if (!email.isEmpty() && !isValidEmail(email)) {
            etEmail.setError("Invalid email format");
            return;
        }

        if (networkManager.isLoggedIn()) {
            networkManager.updateUser(username, email, new NetworkManager.ApiCallback<>() {
                @Override
                public void onSuccess(UserResponse result) {
                    if (currentUser != null) {
                        currentUser.setUsername(username);
                        currentUser.setEmail(email);
                        userStorage.setCurrentUser(currentUser);
                    }

                    Toast.makeText(requireContext(), "Account updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Update failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (currentUser != null) {
                currentUser.setUsername(username);
                currentUser.setEmail(email);
                userStorage.setCurrentUser(currentUser);
                Toast.makeText(requireContext(), "Account updated locally", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void showLogoutConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.log_out)
                .setMessage(R.string.confirm_log_out)
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performLogout() {
        networkManager.logout();
        userStorage.logout();

        Toast.makeText(requireContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void deleteAccount() {
        Toast.makeText(requireContext(), "Account deletion would be implemented here", Toast.LENGTH_SHORT).show();
    }
}