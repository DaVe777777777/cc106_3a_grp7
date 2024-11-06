package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    EditText etUsername, etOldPassword, etNewPassword;
    Button btnUpdateProfile, btnLogout;
    DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize DBHelper and views
        dbHelper = new DBHelper(getActivity());
        etUsername = view.findViewById(R.id.etUsername);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Get the username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("loggedInUser", "Guest"); // Changed key to "loggedInUser"
        etUsername.setText(username);

        // Handle updating profile information
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = etUsername.getText().toString();
                String oldPassword = etOldPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();

                if (newUsername.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a username", Toast.LENGTH_LONG).show();
                    return;
                }

                // Validate old password if new password is being set
                if (!newPassword.isEmpty() && !dbHelper.checkUserCredentials(username, oldPassword)) {
                    Toast.makeText(getActivity(), "Wrong password", Toast.LENGTH_LONG).show();
                    return;
                }

                // Update username if it has changed
                if (!newUsername.equals(username)) {
                    boolean usernameUpdated = dbHelper.updateUsername(username, newUsername);
                    if (usernameUpdated) {
                        Toast.makeText(getActivity(), "Username successfully updated", Toast.LENGTH_LONG).show();

                        // Update SharedPreferences with the new username
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("loggedInUser", newUsername); // Ensure consistent key
                        editor.apply(); // Save changes
                    } else {
                        Toast.makeText(getActivity(), "Failed to update username", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Username not changed", Toast.LENGTH_SHORT).show();
                }

                // Update password if a new password is provided
                if (!newPassword.isEmpty()) {
                    boolean passwordUpdated = dbHelper.updatePassword(newUsername, newPassword);
                    if (passwordUpdated) {
                        Toast.makeText(getActivity(), "Password successfully updated", Toast.LENGTH_LONG).show();

                        // Clear the password fields after updating
                        etOldPassword.setText("");
                        etNewPassword.setText("");
                    } else {
                        Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Password not changed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle logout functionality
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to login screen after logout
                Intent loginIntent = new Intent(getActivity(), ActivityLogin.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                getActivity().finish(); // Close current activity to prevent returning back
                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
