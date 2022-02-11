package com.aravind.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private boolean otpSent = false;

    private final String countrycode = "+91";

    // String for storing our verification id
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText mobileET = findViewById(R.id.mobileET);
        final EditText otpET = findViewById(R.id.otpET);
        final Button actionBtn = findViewById(R.id.actionBtn);

        FirebaseApp.initializeApp(this);

        // creation of FirebaseAuth variable and instantiating
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //setting onClick Listeners to generate OTP
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otpSent){
                    final String getOTP = otpET.getText().toString();

                    if(id.isEmpty()){
                        Toast.makeText(MainActivity.this,"Unable to verify OTP",Toast.LENGTH_SHORT).show();
                    }
                    else{

                        // PhoneAuthCredential is used to verify code from firebase
                        // below line is used for getting credentials from our id and OTP

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id,getOTP);

                        // after getting credential we are calling sign in method
                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser userdetails = Objects.requireNonNull(task.getResult()).getUser();
                                    Toast.makeText(MainActivity.this,"Verified",Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                }
                else{
                   final String getMobile = mobileET.getText().toString();

                   // getting OTP on user phone number
                    PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(firebaseAuth);
                    builder.setPhoneNumber(countrycode + "" + getMobile);
                    builder.setTimeout(60L, TimeUnit.SECONDS);
                    builder.setActivity(MainActivity.this);
                    builder.setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        // this method is used when user receive OTP from Firebase.
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            Toast.makeText(MainActivity.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();


                        }

                        // this method is used when Firebase doesn't send OTP code due to any error or issue
                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(MainActivity.this, "Something went wrong " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                            super.onCodeAutoRetrievalTimeOut(s);

                            otpET.setVisibility(View.VISIBLE);
                            actionBtn.setText("Verify OTP");

                            // when we receive OTP it contains a unique id which we are storing in our string which we have created already
                            id = s;

                            otpSent = true;
                        }
                    });
                    PhoneAuthOptions options = builder.build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
    }
}