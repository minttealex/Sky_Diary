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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountSettingsFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etCurrentPassword, etNewPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseManager.getInstance().getAuth();
        db = FirebaseManager.getInstance().getDb();
        currentUser = auth.getCurrentUser();

        ImageButton buttonBack = view.findViewById(R.id.button_back);
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);
        TextView tvDeleteAccount = view.findViewById(R.id.tv_delete_account);

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null) etUsername.setText(displayName);
            etEmail.setText(currentUser.getEmail());
        }

        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

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

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!email.isEmpty() && !email.equals(currentUser.getEmail())) {
                            currentUser.updateEmail(email)
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            updateFirestoreUser(username, email);
                                        } else {
                                            Toast.makeText(requireContext(), "Email update failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            updateFirestoreUser(username, email);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Profile update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestoreUser(String username, String email) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("email", email);
        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Account updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Account updated locally, but cloud sync failed", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
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
        NoteStorage.getInstance(requireContext()).clearAllNotes();
        ConstellationStorage.getInstance(requireContext()).resetToDefault();
        auth.signOut();
        auth.signInAnonymously();
        Toast.makeText(requireContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void deleteAccount() {
        Toast.makeText(requireContext(), "Account deletion not implemented", Toast.LENGTH_SHORT).show();
    }
}