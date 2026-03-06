package com.example.skydiary;

import android.os.Bundle;
import android.text.TextUtils;
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
            String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Username is required");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }

            // Validate email if provided
            if (!email.isEmpty() && !isValidEmail(email)) {
                etEmail.setError("Invalid email format");
                return;
            }

            btnSignUp.setEnabled(false);
            btnSignUp.setText(R.string.creating_account);

            checkUsernameAvailability(username, new UsernameCheckCallback() {
                @Override
                public void onAvailable() {
                    final String finalEmail;
                    if (!email.isEmpty()) {
                        finalEmail = email;
                    } else {
                        finalEmail = username + "@skydiary.local";
                    }

                    auth.createUserWithEmailAndPassword(finalEmail, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();
                                    assert firebaseUser != null;
                                    firebaseUser.updateProfile(profileUpdates);

                                    Map<String, Object> usernameMap = new HashMap<>();
                                    usernameMap.put("email", finalEmail);
                                    db.collection("usernames").document(username)
                                            .set(usernameMap)
                                            .addOnSuccessListener(aVoid -> {
                                                SyncManager syncManager = new SyncManager(requireContext());
                                                syncManager.uploadLocalNotes(firebaseUser.getUid(), new SyncManager.SyncCallback() {
                                                    @Override
                                                    public void onSuccess(String message) {
                                                        Toast.makeText(requireContext(), "Account created. " + message, Toast.LENGTH_SHORT).show();
                                                        navigateToMain();
                                                    }
                                                    @Override
                                                    public void onError(String error) {
                                                        Toast.makeText(requireContext(), "Account created but sync failed: " + error, Toast.LENGTH_LONG).show();
                                                        navigateToMain();
                                                    }
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                btnSignUp.setEnabled(true);
                                                btnSignUp.setText(R.string.sign_up);
                                                Toast.makeText(requireContext(), "Failed to save username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                firebaseUser.delete(); // Cleanup
                                            });
                                } else {
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText(R.string.sign_up);
                                    String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onTaken() {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText(R.string.sign_up);
                    etUsername.setError("Username already taken");
                }

                @Override
                public void onError(String error) {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText(R.string.sign_up);
                    Toast.makeText(requireContext(), "Error checking username: " + error, Toast.LENGTH_SHORT).show();
                }
            });
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