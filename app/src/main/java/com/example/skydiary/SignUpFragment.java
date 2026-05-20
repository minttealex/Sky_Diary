package com.example.skydiary;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SignUpFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnSignUp;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private boolean isSignUpInProgress = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseManager.getInstance().getAuth();
        db = FirebaseManager.getInstance().getDb();

        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnSignUp.setOnClickListener(v -> {
            if (isSignUpInProgress) {
                return;
            }

            String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

            // Basic validation
            if (username.isEmpty()) {
                etUsername.setError(getString(R.string.username_required));
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.password_required));
                return;
            }
            if (password.length() < 6) {
                etPassword.setError(getString(R.string.password_min_length));
                return;
            }

            setSignUpInProgress(true);

            if (!email.isEmpty()) {
                if (!isValidEmail(email)) {
                    etEmail.setError(getString(R.string.invalid_email));
                    setSignUpInProgress(false);
                    return;
                }

                auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean isNewUser = Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();
                                if (isNewUser) {
                                    proceedSignUp(username, email, password);
                                } else {
                                    etEmail.setError(getString(R.string.email_already_in_use));
                                    setSignUpInProgress(false);
                                }
                            } else {
                                Toast.makeText(requireContext(), R.string.email_check_failed, Toast.LENGTH_SHORT).show();
                                setSignUpInProgress(false);
                            }
                        });
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.no_email_warning_title)
                        .setMessage(R.string.no_email_warning_message)
                        .setPositiveButton(R.string.yes_continue, (dialog, which) -> {
                            String fabricatedEmail = username + "@skydiary.local";
                            proceedSignUp(username, fabricatedEmail, password);
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            setSignUpInProgress(false);
                        })
                        .setOnCancelListener(dialog -> {
                            setSignUpInProgress(false);
                        })
                        .show();
            }
        });
    }

    private void setSignUpInProgress(boolean inProgress) {
        isSignUpInProgress = inProgress;
        btnSignUp.setEnabled(!inProgress);
        btnSignUp.setText(inProgress ? R.string.creating_account : R.string.sign_up);
    }

    private void proceedSignUp(String username, String email, String password) {

        checkUsernameAvailability(username, new UsernameCheckCallback() {
            @Override
            public void onAvailable() {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();

                                if (!email.endsWith("@skydiary.local")) {
                                    assert firebaseUser != null;
                                    firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener(verificationTask -> {
                                                if (verificationTask.isSuccessful()) {
                                                    Toast.makeText(requireContext(), R.string.verification_email_sent, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                assert firebaseUser != null;
                                firebaseUser.updateProfile(profileUpdates);

                                Map<String, Object> usernameMap = new HashMap<>();
                                usernameMap.put("email", email);
                                db.collection("usernames").document(username)
                                        .set(usernameMap)
                                        .addOnSuccessListener(aVoid -> {
                                            SyncManager syncManager = new SyncManager(requireContext());
                                            syncManager.uploadLocalNotes(firebaseUser.getUid(), new SyncManager.SyncCallback() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    Toast.makeText(requireContext(), getString(R.string.account_created) + " " + message, Toast.LENGTH_SHORT).show();
                                                    syncManager.syncConstellationsOnly(firebaseUser.getUid(), new SyncManager.SyncCallback() {
                                                        @Override
                                                        public void onSuccess(String msg) {
                                                        }
                                                        @Override
                                                        public void onError(String error) {
                                                            Log.e("SignUp", "Constellation sync failed: " + error);
                                                        }
                                                    });
                                                    navigateToMain();
                                                }
                                                @Override
                                                public void onError(String error) {
                                                    Toast.makeText(requireContext(), getString(R.string.account_created_sync_failed) + " " + error, Toast.LENGTH_LONG).show();
                                                    navigateToMain();
                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            setSignUpInProgress(false);
                                            Toast.makeText(requireContext(), R.string.username_save_failed, Toast.LENGTH_SHORT).show();
                                            firebaseUser.delete();
                                        });
                            } else {
                                setSignUpInProgress(false);
                                String error = task.getException() != null ? task.getException().getMessage() : getString(R.string.registration_failed);
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onTaken() {
                setSignUpInProgress(false);
                etUsername.setError(getString(R.string.username_taken));
            }

            @Override
            public void onError(String error) {
                setSignUpInProgress(false);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface UsernameCheckCallback {
        void onAvailable();
        void onTaken();
        void onError(String error);
    }

    private void checkUsernameAvailability(String username, UsernameCheckCallback callback) {
        db.collection("usernames").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onTaken();
                    } else {
                        callback.onAvailable();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
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