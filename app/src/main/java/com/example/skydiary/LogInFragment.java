package com.example.skydiary;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LogInFragment extends Fragment {

    private TextInputEditText etUsernameOrEmail, etPassword;
    private MaterialButton btnLogIn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseManager.getInstance().getAuth();
        db = FirebaseManager.getInstance().getDb();

        etUsernameOrEmail = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogIn = view.findViewById(R.id.btn_log_in);
        TextView tvForgotPassword = view.findViewById(R.id.tv_forgot_password);

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnLogIn.setOnClickListener(v -> {
            String input = Objects.requireNonNull(etUsernameOrEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (input.isEmpty()) {
                etUsernameOrEmail.setError(getString(R.string.username_or_email_required));
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.password_required));
                return;
            }

            btnLogIn.setEnabled(false);
            btnLogIn.setText(R.string.logging_in);

            if (input.contains("@")) {
                performSignIn(input, password);
            } else {
                db.collection("usernames").document(input).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String email = documentSnapshot.getString("email");
                                if (email != null) {
                                    performSignIn(email, password);
                                } else {
                                    showError(getString(R.string.invalid_username_or_password));
                                }
                            } else {
                                showError(getString(R.string.invalid_username_or_password));
                            }
                        })
                        .addOnFailureListener(e -> showError(getString(R.string.username_lookup_failed) + e.getMessage()));
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etUsernameOrEmail.getText()).toString().trim();
            if (email.isEmpty() || !isValidEmail(email)) {
                Toast.makeText(requireContext(), R.string.enter_valid_email_for_reset, Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), R.string.password_reset_email_sent, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), R.string.password_reset_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void performSignIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && !user.isEmailVerified() && !email.endsWith("@skydiary.local")) {
                            showVerificationDialog(user);
                            return;
                        }
                        assert user != null;
                        proceedAfterLogin(user);
                    } else {
                        showError(task.getException() != null ? task.getException().getMessage() : getString(R.string.login_failed));
                    }
                });
    }

    private void showVerificationDialog(FirebaseUser user) {
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.log_in);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.email_not_verified)
                .setMessage(R.string.verify_email_prompt)
                .setPositiveButton(R.string.resend_verification, (dialog, which) -> {
                    user.sendEmailVerification();
                    Toast.makeText(requireContext(), R.string.verification_email_sent, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void proceedAfterLogin(FirebaseUser user) {
        SyncManager syncManager = new SyncManager(requireContext());
        syncManager.checkUserHasData(user.getUid(), new SyncManager.DataCheckCallback() {
            @Override
            public void onResult(boolean hasData) {
                if (!isAdded()) return;
                if (hasData) {
                    syncManager.downloadAndReplace(new SyncManager.SyncCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), getString(R.string.login_successful) + " " + message, Toast.LENGTH_SHORT).show();
                            handleConstellations(user, syncManager);
                        }
                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), getString(R.string.login_success_sync_failed) + " " + error, Toast.LENGTH_LONG).show();
                            handleConstellations(user, syncManager);
                        }
                    });
                } else {
                    syncManager.uploadLocalNotes(user.getUid(), new SyncManager.SyncCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), getString(R.string.login_successful) + " " + message, Toast.LENGTH_SHORT).show();
                            handleConstellations(user, syncManager);
                        }
                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), getString(R.string.login_success_sync_failed) + " " + error, Toast.LENGTH_LONG).show();
                            handleConstellations(user, syncManager);
                        }
                    });
                }
            }
            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), getString(R.string.login_success_sync_check_failed) + " " + error, Toast.LENGTH_LONG).show();
                handleConstellations(user, syncManager);
            }
        });
    }

    private void handleConstellations(FirebaseUser user, SyncManager syncManager) {
        syncManager.checkUserHasConstellations(user.getUid(), new SyncManager.DataCheckCallback() {
            @Override
            public void onResult(boolean hasConstellations) {
                Log.d("Login", "checkUserHasConstellations result: " + hasConstellations);
                if (!isAdded()) return;
                if (hasConstellations) {
                    syncManager.downloadConstellations(user.getUid(), new SyncManager.SyncCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.d("Login", "Constellations downloaded: " + msg);
                            ConstellationStorage.getInstance(requireContext()).forceRefreshConstellations();
                            navigateToMain();
                        }
                        @Override
                        public void onError(String error) {
                            Log.e("Login", "Constellation download failed: " + error);
                            ConstellationStorage.getInstance(requireContext()).forceRefreshConstellations(); // refresh anyway
                            navigateToMain();
                        }
                    });
                } else {
                    syncManager.uploadConstellations(user.getUid(), new SyncManager.SyncCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.d("Login", "Constellations uploaded: " + msg);
                            ConstellationStorage.getInstance(requireContext()).forceRefreshConstellations();
                            navigateToMain();
                        }
                        @Override
                        public void onError(String error) {
                            Log.e("Login", "Constellation upload failed: " + error);
                            ConstellationStorage.getInstance(requireContext()).forceRefreshConstellations();
                            navigateToMain();
                        }
                    });
                }
            }
            @Override
            public void onError(String error) {
                Log.e("Login", "Constellation check failed: " + error);
                ConstellationStorage.getInstance(requireContext()).forceRefreshConstellations();
                navigateToMain();
            }
        });
    }

    private void showError(String error) {
        if (!isAdded()) return;
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.log_in);
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}