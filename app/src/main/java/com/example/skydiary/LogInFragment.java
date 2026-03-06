package com.example.skydiary;

import android.os.Bundle;
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

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnLogIn.setOnClickListener(v -> {
            String input = Objects.requireNonNull(etUsernameOrEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (input.isEmpty()) {
                etUsernameOrEmail.setError("Username or email is required");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
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
                                    showError("Invalid username or password");
                                }
                            } else {
                                showError("Invalid username or password");
                            }
                        })
                        .addOnFailureListener(e -> showError("Error checking username: " + e.getMessage()));
            }
        });
    }

    private void performSignIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        SyncManager syncManager = new SyncManager(requireContext());
                        assert user != null;
                        syncManager.checkUserHasData(user.getUid(), new SyncManager.DataCheckCallback() {
                            @Override
                            public void onResult(boolean hasData) {
                                if (hasData) {
                                    syncManager.downloadAndReplace(new SyncManager.SyncCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            Toast.makeText(requireContext(), "Login successful. " + message, Toast.LENGTH_SHORT).show();
                                            navigateToMain();
                                        }
                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(requireContext(), "Login successful but sync failed: " + error, Toast.LENGTH_LONG).show();
                                            navigateToMain();
                                        }
                                    });
                                } else {
                                    syncManager.uploadLocalNotes(user.getUid(), new SyncManager.SyncCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            Toast.makeText(requireContext(), "Login successful. " + message, Toast.LENGTH_SHORT).show();
                                            navigateToMain();
                                        }
                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(requireContext(), "Login successful but sync failed: " + error, Toast.LENGTH_LONG).show();
                                            navigateToMain();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Login successful but sync check failed: " + error, Toast.LENGTH_LONG).show();
                                navigateToMain();
                            }
                        });
                    } else {
                        showError(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    private void showError(String error) {
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.log_in);
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
    }
}