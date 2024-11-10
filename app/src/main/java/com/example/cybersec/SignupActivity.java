package com.example.cybersec;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword;
    private Button buttonSignup;
    private DatabaseHelper databaseHelper;
    TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        editTextName = findViewById(R.id.name);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonSignup = findViewById(R.id.submit);
        loginRedirect=findViewById(R.id.loginRe);
        databaseHelper = new DatabaseHelper(this);

        //redirect to login page

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (databaseHelper.checkUserExists(email)) {
                        Toast.makeText(SignupActivity.this, "You are already signed up. Please log in.", Toast.LENGTH_SHORT).show();
                    } else {
                        boolean isInserted = databaseHelper.insertUser(name, email, password);
                        if (isInserted) {
                            Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                            finish();  // Close activity after signup
                        } else {
                            Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
}
