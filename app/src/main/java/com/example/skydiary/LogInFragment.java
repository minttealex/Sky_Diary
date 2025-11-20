package com.example.skydiary;

import android.os.Bundle;
import android.util.Log;
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

                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MainFragment())
                            .commit();
                }

                @Override
                public void onError(String error) {
                    btnLogIn.setEnabled(true);
                    btnLogIn.setText(R.string.log_in);

                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}