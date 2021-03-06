package com.learning.aman.mapapi.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.learning.aman.mapapi.MapsActivity;
import com.learning.aman.mapapi.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mName, mEmail, mPassword;
    private Button mSubmit;
    private ProgressDialog mProgressBar;

    //Firebase Auth
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Database Reference
    private DatabaseReference mUserDetails = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        onClick();

    }

    private void init() {
        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.regEmail);
        mPassword =  findViewById(R.id.regPassword);
        mSubmit = findViewById(R.id.regButton);
    }

    private void onClick() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mProgressBar = new ProgressDialog(RegisterActivity.this);
                    mProgressBar.setMessage("Registering....");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();
                    register(name, email, password);

                }else {
                    Toast.makeText(RegisterActivity.this, "Enter Vaild email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register(final String name, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    HashMap<String,String> userDetails = new HashMap<>();
                    userDetails.put("name",name);
                    userDetails.put("email",email);
                    userDetails.put("password",password);

                    mUserDetails.child("Users").child(uid).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Creating Account Successfull", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
                                finish();
                                mProgressBar.dismiss();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(RegisterActivity.this,"Creating Account Failed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
