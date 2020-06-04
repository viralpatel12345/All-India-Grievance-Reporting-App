package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends BaseActivity {

    TextView email_textView;
    EditText email;
    Button forgotPasswordButton;
    ProgressDialog progressDialog;
    ConstraintLayout constraintLayout;
    String e;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("FORGOT PASSWORD");
        setContentView(R.layout.activity_forgot_password);

        constraintLayout     = findViewById(R.id.constraintLayout);
        email_textView       = findViewById(R.id.email_textView);
        email                = findViewById(R.id.email_address);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        progressDialog   = new ProgressDialog(ForgotPassword.this);
        progressDialog.setCancelable(false);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(),0);
                e=email.getText().toString().trim();
                if(e.isEmpty())
                {
                    email.setError("EMAIL REQUIRED *");
                }
                else
                {
                    if(isConnected(ForgotPassword.this)!=true)
                    {
                        Toast.makeText(ForgotPassword.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        progressDialog.setMessage("Please wait");
                        progressDialog.show();
                        FirebaseAuth.getInstance().sendPasswordResetEmail(e)
                                                  .addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                              @Override
                              public void onComplete(@NonNull Task<Void> task)
                              {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        success_alert(ForgotPassword.this,"Email has been successfully sent.\n\nClick the link contained in the email and you will be forwarded to page where you can reset your password.");
                                    }
                                    else
                                    {
                                         progressDialog.dismiss();
                                         Toast.makeText(ForgotPassword.this, "User not found !", Toast.LENGTH_LONG).show();
                                    }
                              }
                        });
                    }
                }
             }
        });

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
                startActivity(new Intent(ForgotPassword.this, ForgotPassword.class));
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
    public void onBackPressed()
    {
        startActivity(new Intent(ForgotPassword.this,LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
