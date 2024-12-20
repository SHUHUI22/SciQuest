package com.example.sciquest;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText ETEmail, ETPassword;
    private TextView TVSignUp, TVForgotPassword;
    private Button BtnLogin;
    private ImageButton BtnTogglePassword;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        ETEmail = findViewById(R.id.ETEmail);
        ETPassword = findViewById(R.id.ETPassword);
        TVSignUp = findViewById(R.id.TVSignUp);
        TVForgotPassword = findViewById(R.id.TVForgotPassword);
        BtnLogin = findViewById(R.id.BtnLogin);
        BtnTogglePassword = findViewById(R.id.BtnTogglePassword);

        // Password Visibility Toggle
        BtnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                ETPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                BtnTogglePassword.setImageResource(R.drawable.visibility_off_icon); // Change to 'visibility_off' icon
            } else {
                // Show password
                ETPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                BtnTogglePassword.setImageResource(R.drawable.visibility_icon); // Change to 'visibility' icon
            }
            // Move cursor to the end of the text
            ETPassword.setSelection(ETPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Login
        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = ETEmail.getText().toString();
                String password = ETPassword.getText().toString();

                if (email.isEmpty()){
                    ETEmail.setError(getString(R.string.error_empty_email));
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ETEmail.setError(getString(R.string.error_invalid_email));
                    return;
                }
                if (password.isEmpty()){
                    ETPassword.setError(getString(R.string.error_empty_password));
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login failed.Please check your email and password.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Navigate to Sign Up Activity
        TVSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Forgot Password Activity
        TVForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

    }
}