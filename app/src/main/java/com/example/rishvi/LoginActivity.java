package com.example.rishvi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtLoginEmail;
    private EditText edtLoginPassword;

    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = new DatabaseHelper(this);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());

        findViewById(R.id.txtRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = edtLoginEmail.getText().toString()
                .trim().toLowerCase(java.util.Locale.ROOT);
        String password = edtLoginPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = database.loginUser(email, password);

        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this,
                        "Incorrect email or password",
                        Toast.LENGTH_LONG).show();
                return;
            }

            int userId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );
            int goal = cursor.getInt(
                    cursor.getColumnIndexOrThrow("daily_goal")
            );

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_NAME", name);
            intent.putExtra("DAILY_GOAL", goal);

            startActivity(intent);
            finish();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}
