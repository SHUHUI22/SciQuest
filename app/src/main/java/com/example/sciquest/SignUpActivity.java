package com.example.sciquest;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText ETUsername, ETEmail, ETPassword, ETAge;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button BtnSignUp;
    private TextView TVLogin;
    private String userID;
    private ImageButton BtnTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ETUsername = findViewById(R.id.ETUsername);
        ETEmail = findViewById(R.id.ETEmail);
        ETPassword = findViewById(R.id.ETPassword);
        ETAge = findViewById(R.id.ETAge);
        radioGroupGender = findViewById(R.id.RGGender);
        radioButtonMale = findViewById(R.id.RBMale);
        radioButtonFemale = findViewById(R.id.RBFemale);
        BtnSignUp = findViewById(R.id.BtnSignUp);
        TVLogin = findViewById(R.id.TVLogin);
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

        // Sign up an account
        BtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ETUsername.getText().toString();
                String email = ETEmail.getText().toString();
                String password = ETPassword.getText().toString();
                String age = ETAge.getText().toString();
                String gender = getSelectedGender();

                if (username.isEmpty()){
                    ETUsername.setError(getString(R.string.error_empty_username));
                    return;
                }
                if (email.isEmpty()){
                    ETEmail.setError(getString(R.string.error_empty_email));
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    ETEmail.setError(getString(R.string.error_invalid_email));
                    return;
                }
                if(!isValid(password)){
                    ETPassword.setError(getString(R.string.error_invalid_password));
                    return;
                }
                if (password.isEmpty()){
                    ETPassword.setError(getString(R.string.error_empty_password));
                    return;
                }
                if (age.isEmpty()){
                    ETAge.setError(getString(R.string.error_empty_age));
                    return;
                }
                if(gender==null){
                    Toast.makeText(SignUpActivity.this, getString(R.string.error_empty_gender), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Get the user ID from the authenticated user
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user!=null){
                                        userID = user.getUid();
                                        // Store user details in Firestore
                                        storeUserDetailsInFirestore(username,email,age,gender);
                                    }
                                    Toast.makeText(SignUpActivity.this, "Sign Up Successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                                    finish();
                                }
                                else {
                                    Toast.makeText(SignUpActivity.this, "Sign Up Failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Navigate to Log In Activity
        TVLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isValid(String password){
        // Password must be at least 8 characters long
        // It must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit (number)
        return password.length()>=8 && containUpperCase(password) && containLowerCase(password) && containDigit(password);
    }

    private boolean containDigit(String password) {
        for(char c:password.toCharArray()){
            if(Character.isDigit(c)){
                return true;
            }
        }
        return false;
    }

    private boolean containLowerCase(String password) {
        for(char c:password.toCharArray()){
            if(Character.isLowerCase(c)){
                return true;
            }
        }
        return false;
    }

    private boolean containUpperCase(String password) {
        for(char c:password.toCharArray()){
            if(Character.isUpperCase(c)){
                return true;
            }
        }
        return false;
    }

    private void storeUserDetailsInFirestore (String username, String email, String age, String gender){
        // Create a map to store user details
        Map<String, Object> user = new HashMap<>();
        user.put( "username" , username);
        user.put( "email" , email);
        user.put( "age" , Integer.parseInt(age));
        user.put( "gender" , gender);

        // Add a new document with the user ID as the document ID
        db.collection( "Users" ).document(userID).set(user);
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId == - 1 ) {
            // No gender selected
            return null;
        }
        else {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
    }
}
