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

import java.util.Objects;

public class SignUpFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnSignUp;
    private NetworkManager networkManager;
    private UserStorage userStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkManager = NetworkManager.getInstance(requireContext());
        userStorage = UserStorage.getInstance(requireContext());

        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);

        view.findViewById(R.id.button_back).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

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

            if (email.isEmpty()) {
                email = "";
            }

            btnSignUp.setEnabled(false);
            btnSignUp.setText(R.string.creating_account);

            networkManager.register(username, email, password, new NetworkManager.ApiCallback<>() {
                @Override
                public void onSuccess(UserResponse result) {
                    User user = new User(result.getUsername(), result.getEmail());
                    userStorage.setCurrentUser(user);

                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MainFragment())
                            .commit();
                }

                @Override
                public void onError(String error) {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText(R.string.sign_up);

                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}