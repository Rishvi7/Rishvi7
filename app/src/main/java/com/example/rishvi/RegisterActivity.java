package com.example.rishvi;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtGoal;

    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = new DatabaseHelper(this);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtGoal = findViewById(R.id.edtGoal);

        findViewById(R.id.btnRegister).setOnClickListener(v -> register());

        findViewById(R.id.txtLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void register() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString()
                .trim().toLowerCase(java.util.Locale.ROOT);
        String password = edtPassword.getText().toString();
        String goalText = edtGoal.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()
                || password.isEmpty() || goalText.isEmpty()) {
            Toast.makeText(this, "Complete all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email");
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Use at least 6 characters");
            return;
        }

        int goal;

        try {
            goal = Integer.parseInt(goalText);
        } catch (NumberFormatException e) {
            edtGoal.setError("Enter a valid number");
            return;
        }

        if (goal < 500 || goal > 10000) {
            edtGoal.setError("Enter a goal between 500 and 10000");
            return;
        }

        long userId = database.registerUser(
                name, email, password, goal
        );

        if (userId == -1) {
            edtEmail.setError("This email may already be registered");
            Toast.makeText(this,
                    "Registration failed. Try another email.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Account created successfully!",
                Toast.LENGTH_SHORT).show();

        openMain(userId, name, goal);
    }

    private void openMain(long userId, String name, int goal) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", (int) userId);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("DAILY_GOAL", goal);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}
