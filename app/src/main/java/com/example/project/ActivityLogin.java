package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityLogin extends AppCompatActivity {
    Button btnLogin, btnRegister;
    EditText etUsername, etPwd;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPwd = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString().trim();
                String password = etPwd.getText().toString().trim();
                Log.d("LoginAttempt", "Username: " + username + ", Password: " + password);
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ActivityLogin.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isLoggedIn = dbHelper.checkUserCredentials(username, password);
                    if (isLoggedIn) {
                        // Retrieve user ID based on username
                        Integer userIdInt = dbHelper.getUserId(username);

                        // Check if userIdInt is not null before converting and saving
                        if (userIdInt != null) {
                            String userId = String.valueOf(userIdInt); // Convert Integer to String

                            // Store user data in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("loggedInUser", username);
                            editor.putString("userId", userId); // Save user ID as a String
                            editor.putString("username", username);
                            editor.apply();

                            // Log user information for debugging purposes
                            Log.d("LoginSuccess", "User logged in: Username=" + username + ", UserID=" + userId);

                            // Redirect to home activity
                            Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ActivityLogin.this, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ActivityLogin.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
                startActivity(intent);
            }
        });
    }
}
