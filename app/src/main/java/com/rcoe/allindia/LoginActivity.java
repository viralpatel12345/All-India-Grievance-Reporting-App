package com.rcoe.allindia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.regex.Pattern;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LoginActivity extends BaseActivity {

    private final static String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
    private Button login,register,forgotPassword;
    private EditText email,password;
    private TextView show_hide_password,version;
    private DatabaseReference workersReference;
    private boolean password_visibility = false;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ProgressDialog pb;
    private String user_email,user_password;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Sign In");

        constraintLayout   = findViewById(R.id.layout);
        version            = findViewById(R.id.version);
        email              = findViewById(R.id.email);
        password           = findViewById(R.id.password);
        show_hide_password = findViewById(R.id.show_hide_password);
        register           = findViewById(R.id.register);
        login              = findViewById(R.id.loginbtn);
        forgotPassword     = findViewById(R.id.forgotPassword);
        workersReference   = FirebaseDatabase.getInstance().getReference().child("Workers");
        firebaseAuth       = FirebaseAuth.getInstance();
        pb                 = new ProgressDialog(this);
        user               = firebaseAuth.getCurrentUser();

        version.setText("Version 2.1");

        if(user != null)
        {
            pb.setMessage("Redirecting");
            pb.show();
            workersReference.child(user.getUid())
                    .addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.exists())
                            {
                                pb.dismiss();
                                Intent intent = new Intent(LoginActivity.this,WorkerHome.class);
                                intent.putExtra("device_token","token");
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            else
                            {
                                pb.dismiss();
                                Intent intent = new Intent(LoginActivity.this,Home.class);
                                intent.putExtra("device_token","token");
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        show_hide_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_visibility == false)
                {
                    password_visibility = true;
                    show_hide_password.setText("HIDE PASSWORD");
                    password.setTransformationMethod(null);
                }
                else
                {
                    password_visibility = false;
                    show_hide_password.setText("SHOW PASSWORD");
                    password.setTransformationMethod(new PasswordTransformationMethod());

                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected(LoginActivity.this)!=true)
                {
                    Toast.makeText(LoginActivity.this,"Please check your Internet Connection",Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (validate())
                    {
                        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(),0);
                        pb.setCancelable(false);
                        pb.setMessage("Authenticating");
                        pb.show();

                        firebaseAuth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                   workersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                   .addValueEventListener(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                       {
                                           if(dataSnapshot.exists())
                                           {
                                               pb.dismiss();
                                               Intent intent = new Intent(LoginActivity.this,WorkerHome.class);
                                               intent.putExtra("device_token","token");
                                               startActivity(intent);
                                               overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                           }
                                           else
                                           {
                                               pb.dismiss();
                                               Intent intent = new Intent(LoginActivity.this,Home.class);
                                               intent.putExtra("device_token","token");
                                               startActivity(intent);
                                               finish();
                                               overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                           }
                                       }
                                       @Override
                                       public void onCancelled(@NonNull DatabaseError databaseError)
                                       {

                                       }
                                   });
                                }
                                else
                                {
                                    pb.dismiss();
                                    Toast.makeText(LoginActivity.this, "Incorrect Email or Password", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }

        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

    }

    private boolean validate()
    {
        boolean valid = true;
        user_email    = email.getText().toString().trim();
        user_password = password.getText().toString().trim();

        if(Pattern.compile(PASSWORD_PATTERN).matcher(user_password).matches() != true)
        {
            if (user_password.isEmpty()) {
                password.setError("PASSWORD REQUIRED *");
                password.setFocusable(true);
            }else {
                passwordAlertdialogBox(LoginActivity.this);
            }
            valid = false;
        }
        else
        {
            password.setError(null);
        }

        if(Patterns.EMAIL_ADDRESS.matcher(user_email).matches() != true)
        {
            if(user_email.isEmpty())
            {
                email.setError("EMAIL REQUIRED *");
                email.setFocusable(true);
            }
            else
            {
                email.setError("Invalid Email Format");
                email.setFocusable(true);
            }
            valid = false;

        }
        else
        {
            email.setError(null);
        }
        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.front_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.refresh:
                startActivity(new Intent(LoginActivity.this,LoginActivity.class));
                finish();
                break;

            case R.id.share:
                share();
                break;

            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.alert(R.layout.quit_alert,LoginActivity.this,firebaseAuth);
    }

}
