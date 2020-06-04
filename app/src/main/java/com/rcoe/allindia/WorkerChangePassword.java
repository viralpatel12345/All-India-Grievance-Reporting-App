package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.regex.Pattern;

public class WorkerChangePassword extends BaseActivity {

    private final static String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
    private EditText oldPassword,newPassword,confirmNewPassword;
    private Button changePasswordButton;
    private FirebaseUser user;
    private String oldP,newP,confirmP;
    private AuthCredential credential;
    private FirebaseAuth auth;
    private boolean password_visibility=false;
    private TextView sh1,sh2,sh3;
    private ProgressDialog progressDialog;
    private ConstraintLayout constraintLayout;
    private String postal_code=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_change_password);
        getSupportActionBar().setTitle("CHANGE PASSWORD");

        postal_code      = getIntent().getStringExtra("postal_code");
        progressDialog   = new ProgressDialog(WorkerChangePassword.this);
        progressDialog.setMessage("Authenticating ");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        constraintLayout     = findViewById(R.id.constraintLayout);
        oldPassword          = findViewById(R.id.oldpass);
        newPassword          = findViewById(R.id.newpassword);
        confirmNewPassword   = findViewById(R.id.confirmnewpassword);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        sh1                  = findViewById(R.id.sh1);
        sh2                  = findViewById(R.id.sh2);
        sh3                  = findViewById(R.id.sh3);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                {
                    InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(),0);
                    progressDialog.show();
                    credential = EmailAuthProvider.getCredential(user.getEmail(),oldPassword.getText().toString().trim());
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                progressDialog.setMessage("Updating your password ");
                                user.updatePassword(newP).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            auth.signOut();
                                            progressDialog.dismiss();
                                            success_alert();
                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            Toast.makeText(WorkerChangePassword.this,"Password Not Updated !",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(WorkerChangePassword.this,"Authentication Failed !",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });

        sh1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_visibility == false)
                {
                    password_visibility = true;
                    sh1.setText("Hide Password");
                    oldPassword.setTransformationMethod(null);
                }
                else
                {
                    password_visibility = false;
                    sh1.setText("Show Password");
                    oldPassword.setTransformationMethod(new PasswordTransformationMethod());

                }
            }
        });

        sh2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_visibility == false)
                {
                    password_visibility = true;
                    sh2.setText("Hide Password");
                    newPassword.setTransformationMethod(null);
                }
                else
                {
                    password_visibility = false;
                    sh2.setText("Show Password");
                    newPassword.setTransformationMethod(new PasswordTransformationMethod());

                }
            }
        });

        sh3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_visibility == false)
                {
                    password_visibility = true;
                    sh3.setText("Hide Password");
                    confirmNewPassword.setTransformationMethod(null);
                }
                else
                {
                    password_visibility = false;
                    sh3.setText("Show Password");
                    confirmNewPassword.setTransformationMethod(new PasswordTransformationMethod());

                }
            }
        });
    }


    void success_alert()
    {
        progressDialog.dismiss();
        LayoutInflater inflater = LayoutInflater.from(WorkerChangePassword.this);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert,null);
        TextView tv = view.findViewById(R.id.message);
        tv.setText("Password Successfully Updated !");
        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerChangePassword.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                startActivity(new Intent(WorkerChangePassword.this, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    public boolean validate() {
        boolean valid = true;

        oldP = oldPassword.getText().toString().trim();
        newP = newPassword.getText().toString().trim();
        confirmP = confirmNewPassword.getText().toString().trim();

        if(Pattern.compile(PASSWORD_PATTERN).matcher(oldP).matches() != true) {
            if (oldP.isEmpty()) {
                oldPassword.setError("OLD PASSWORD REQUIRED *");
                oldPassword.setFocusable(true);
            } else {
                passwordAlertdialogBox(WorkerChangePassword.this);
            }
            valid = false;
        } else {
            oldPassword.setError(null);
        }

        if(Pattern.compile(PASSWORD_PATTERN).matcher(newP).matches() != true) {
            if (newP.isEmpty()) {
                newPassword.setError("NEW PASSWORD REQUIRED *");
                newPassword.setFocusable(true);
            } else {
                passwordAlertdialogBox(WorkerChangePassword.this);
            }
            valid = false;
        } else {
            newPassword.setError(null);
        }

        if(!(newP.equals(confirmP))) {
            confirmNewPassword.setError("PASSWORD MISMATCH");
            confirmNewPassword.setFocusable(true);
            valid = false;

        } else {
            confirmNewPassword.setError(null);
        }
        if(newP.equals(oldP) && !(newP.isEmpty() && oldP.isEmpty()))
        {
            newPassword.setError("New Password should be different than Old Password");
            newPassword.setFocusable(true);
            valid=false;
        }
        else{
            newPassword.setError(null);
        }

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.refresh:
                startActivity(new Intent(WorkerChangePassword.this,WorkerChangePassword.class));
                finish();
                break;

            case R.id.share:
                share();
                break;

            case R.id.home:
                startActivity(new Intent(WorkerChangePassword.this,WorkerHome.class));
                finish();
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,WorkerChangePassword.this,auth);
                break;

            case R.id.solved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerChangePassword.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status","solved");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.unsolved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerChangePassword.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status","unsolved");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.search_complaint :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerChangePassword.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.profile :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerChangePassword.this,WorkerProfile.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.change_password :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerChangePassword.this,WorkerChangePassword.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(WorkerChangePassword.this,WorkerHome.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
