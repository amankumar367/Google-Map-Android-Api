package com.learning.aman.mapapi.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.learning.aman.mapapi.MapsActivity;
import com.learning.aman.mapapi.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLogin, mRegister;
    private ProgressDialog mProgressBar;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        onClick();

    }

    private void init() {
        mEmail = findViewById(R.id.email);
        mPassword =  findViewById(R.id.password);
        mLogin = findViewById(R.id.loginButton);
        mRegister = findViewById(R.id.register_button);
    }

    private void onClick() {
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mProgressBar = new ProgressDialog(LoginActivity.this);
                    mProgressBar.setMessage("Logging....");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();
                    login(email, password);

                }else {
                    Toast.makeText(LoginActivity.this, "Enter Valid email & password", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Log.e("login","success");
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                    mProgressBar.dismiss();
                }
                else{
                    Log.e("login","failed");
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Login Failed - " + e, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });

                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Log.e("LoginActivity","Not Logged In");
        }else {
            Log.e("LoginActivity","Already Login");
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();

        }
    }
}
