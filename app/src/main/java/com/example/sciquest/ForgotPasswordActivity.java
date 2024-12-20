package com.example.sciquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView TVLogin;
    private Button BtnSendLink;
    private EditText ETEmail;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TVLogin = findViewById(R.id.TVLogin);
        BtnSendLink = findViewById(R.id.BtnSendLink);
        ETEmail = findViewById(R.id.ETEmail);

        // Send link to user email for password reset
        BtnSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ETEmail.getText().toString();
                if(email.isEmpty()){
                    ETEmail.setError(getString(R.string.error_empty_email));
                }
                else {
                    resetPassword(email);
                }
            }
        });

        // Navigate to Log In Activity
        TVLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Reset password link has been sent to your email.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Toast.makeText(ForgotPasswordActivity.this, "Sending reset password link failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}