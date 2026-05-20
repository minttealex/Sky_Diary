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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountSettingsFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etCurrentPassword, etNewPassword;
    private MaterialButton btnUpdate;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private boolean isUpdating = false;

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
        btnUpdate = view.findViewById(R.id.btn_update);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);
        TextView tvDeleteAccount = view.findViewById(R.id.tv_delete_account);

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null) etUsername.setText(displayName);
            etEmail.setText(currentUser.getEmail());
        } else {
            Toast.makeText(requireContext(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnUpdate.setOnClickListener(v -> {
            if (isUpdating) return;
            updateAccount();
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        tvDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    private void setUpdating(boolean updating) {
        isUpdating = updating;
        btnUpdate.setEnabled(!updating);
    }

    private void updateAccount() {
        String newUsername = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String newEmail = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String currentPassword = Objects.requireNonNull(etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();

        // Basic validation
        if (newUsername.isEmpty()) {
            etUsername.setError(getString(R.string.username_required));
            return;
        }

        if (!newEmail.isEmpty() && !isValidEmail(newEmail)) {
            etEmail.setError(getString(R.string.invalid_email));
            return;
        }

        if (currentUser == null) {
            Toast.makeText(requireContext(), R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            return;
        }

        setUpdating(true);

        String oldUsername = currentUser.getDisplayName();
        String oldEmail = currentUser.getEmail();

        if (!newUsername.equals(oldUsername)) {
            db.collection("usernames").document(newUsername).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            etUsername.setError(getString(R.string.username_taken));
                            setUpdating(false);
                        } else {
                            updateUsernameAndProceed(newUsername, oldUsername, newEmail, oldEmail, currentPassword, newPassword);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), R.string.username_check_failed, Toast.LENGTH_SHORT).show();
                        setUpdating(false);
                    });
        } else {
            proceedWithEmailAndPasswordUpdate(newEmail, oldEmail, currentPassword, newPassword);
        }
    }

    private void updateUsernameAndProceed(String newUsername, String oldUsername,
                                          String newEmail, String oldEmail,
                                          String currentPassword, String newPassword) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update usernames collection
                        if (oldUsername != null) {
                            db.collection("usernames").document(oldUsername).delete();
                        }
                        Map<String, Object> usernameMap = new HashMap<>();
                        usernameMap.put("email", currentUser.getEmail());
                        db.collection("usernames").document(newUsername)
                                .set(usernameMap)
                                .addOnSuccessListener(aVoid -> {
                                    proceedWithEmailAndPasswordUpdate(newEmail, oldEmail, currentPassword, newPassword);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), R.string.username_save_failed, Toast.LENGTH_SHORT).show();
                                    setUpdating(false);
                                });
                    } else {
                        Toast.makeText(requireContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                        setUpdating(false);
                    }
                });
    }

    private void proceedWithEmailAndPasswordUpdate(String newEmail, String oldEmail,
                                                   String currentPassword, String newPassword) {
        // Flags to track completion
        final boolean[] emailUpdateDone = {newEmail.isEmpty() || newEmail.equals(oldEmail)};
        final boolean[] passwordUpdateDone = {currentPassword.isEmpty() || newPassword.isEmpty()};

        // Helper to finish update when both are done
        Runnable checkFinish = () -> {
            if (emailUpdateDone[0] && passwordUpdateDone[0]) {
                Toast.makeText(requireContext(), R.string.account_updated, Toast.LENGTH_SHORT).show();
                setUpdating(false);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };

        // Update email if changed
        if (!newEmail.isEmpty() && !newEmail.equals(oldEmail)) {
            if (currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), R.string.password_required_for_email_change, Toast.LENGTH_SHORT).show();
                setUpdating(false);
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, currentPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (!reAuthTask.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.reauthentication_failed, Toast.LENGTH_SHORT).show();
                            setUpdating(false);
                            return;
                        }

                        auth.fetchSignInMethodsForEmail(newEmail)
                                .addOnCompleteListener(emailCheckTask -> {
                                    if (!emailCheckTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), R.string.email_check_failed, Toast.LENGTH_SHORT).show();
                                        setUpdating(false);
                                        return;
                                    }

                                    boolean isNewUser = Objects.requireNonNull(emailCheckTask.getResult().getSignInMethods()).isEmpty();
                                    if (!isNewUser) {
                                        etEmail.setError(getString(R.string.email_already_in_use));
                                        setUpdating(false);
                                        return;
                                    }

                                    currentUser.updateEmail(newEmail)
                                            .addOnCompleteListener(emailTask -> {
                                                if (emailTask.isSuccessful()) {
                                                    // Send verification email if not a local fabricated email
                                                    if (!newEmail.endsWith("@skydiary.local")) {
                                                        currentUser.sendEmailVerification()
                                                                .addOnCompleteListener(verificationTask -> {
                                                                    if (verificationTask.isSuccessful()) {
                                                                        Toast.makeText(requireContext(), R.string.verification_email_sent, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }

                                                    // Update email in Firestore users collection
                                                    db.collection("users").document(currentUser.getUid())
                                                            .update("email", newEmail)
                                                            .addOnFailureListener(e -> {
                                                                // Non-critical, just log
                                                            });

                                                    emailUpdateDone[0] = true;
                                                    checkFinish.run();
                                                } else {
                                                    Toast.makeText(requireContext(), R.string.email_update_failed, Toast.LENGTH_SHORT).show();
                                                    setUpdating(false);
                                                }
                                            });
                                });
                    });
        } else {
            emailUpdateDone[0] = true;
            checkFinish.run();
        }

        // Update password if provided
        if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, currentPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (!reAuthTask.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.reauthentication_failed, Toast.LENGTH_SHORT).show();
                            setUpdating(false);
                            return;
                        }

                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(passwordTask -> {
                                    if (passwordTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), R.string.password_updated, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), R.string.password_update_failed, Toast.LENGTH_SHORT).show();
                                    }
                                    passwordUpdateDone[0] = true;
                                    checkFinish.run();
                                });
                    });
        } else {
            passwordUpdateDone[0] = true;
            checkFinish.run();
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
        NoteStorage.getInstance(requireContext()).clearAllNotes();
        ConstellationStorage.getInstance(requireContext()).resetToDefault();
        auth.signOut();
        auth.signInAnonymously();
        Toast.makeText(requireContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void deleteAccount() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_confirmation_message)
                .setPositiveButton(R.string.delete_account, (dialog, which) -> performAccountDeletion())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performAccountDeletion() {
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        db.collection("notes").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    String username = currentUser.getDisplayName();
                    if (username != null) {
                        db.collection("usernames").document(username).delete();
                    }
                    db.collection("users").document(uid).delete();

                    currentUser.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    NoteStorage.getInstance(requireContext()).clearAllNotes();
                                    ConstellationStorage.getInstance(requireContext()).resetToDefault();
                                    auth.signInAnonymously();
                                    Toast.makeText(requireContext(), R.string.account_deleted, Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                } else {
                                    Toast.makeText(requireContext(), R.string.account_deletion_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), R.string.account_deletion_failed, Toast.LENGTH_SHORT).show());
    }
}