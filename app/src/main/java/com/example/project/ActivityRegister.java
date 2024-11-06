package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityRegister extends AppCompatActivity {
    EditText etUser, etPwd, etRepwd;
    Button btnRegister, btnGoToLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.etUsername);
        etPwd = findViewById(R.id.etPassword);
        etRepwd = findViewById(R.id.etRePassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnLogin);
        dbHelper = new DBHelper(this);


        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityRegister.this, ActivityLogin.class);
                startActivity(intent);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = etUser.getText().toString();
                String pwd = etPwd.getText().toString();
                String rePwd = etRepwd.getText().toString();


                if (user.isEmpty() || pwd.isEmpty() || rePwd.isEmpty()) {
                    Toast.makeText(ActivityRegister.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
                } else {

                    if (pwd.equals(rePwd)) {

                        if (dbHelper.checkUsername(user)) {
                            Toast.makeText(ActivityRegister.this, "User Already Exists", Toast.LENGTH_LONG).show();
                        } else {

                            boolean isRegistered = dbHelper.insertUser(user, pwd);
                            if (isRegistered) {
                                Toast.makeText(ActivityRegister.this, "User Registered Successfully", Toast.LENGTH_LONG).show();

                                etUser.setText("");
                                etPwd.setText("");
                                etRepwd.setText("");
                            } else {
                                Toast.makeText(ActivityRegister.this, "User Registration Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(ActivityRegister.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
