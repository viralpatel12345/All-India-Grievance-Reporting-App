package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class Home extends BaseActivity {

    public static final int REQUEST_LOCATION = 1;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String currentUserID;
    private DatabaseReference userRef;
    private TextView name,email,state,tc,tsc,tusc,textView_tc,textView_tsc,textView_tusc;
    private int total=0,solved=0,unsolved=0;
    private ProgressBar progressBar;
    private CircleImageView profile;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("HOME");

        firebaseAuth      = FirebaseAuth.getInstance();
        currentUser       = firebaseAuth.getCurrentUser();
        currentUserID     = currentUser.getUid();
        userRef           = FirebaseDatabase.getInstance().getReference().child("user");
        progressBar       = findViewById(R.id.progressBar);
        name              = findViewById(R.id.name);
        email             = findViewById(R.id.email);
        state             = findViewById(R.id.state);
        profile           = findViewById(R.id.img);
        tc                = findViewById(R.id.total_complaints);
        tsc               = findViewById(R.id.total_solved_complaints);
        tusc              = findViewById(R.id.total_unsolved_complaints);
        textView_tc       = findViewById(R.id.final_tc_tv);
        textView_tsc      = findViewById(R.id.final_tsc_tv);
        textView_tusc      = findViewById(R.id.final_tusc_tv);

        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        if(getIntent().hasExtra("device_token"))
        {
            saveToken();
        }
        else
        {
            fetchDataFromDatabase();
        }

        textView_tc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Home.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                return true;
            }
        });

        textView_tsc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Home.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                return true;
            }
        });

        textView_tusc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Home.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                return true;
            }
        });

    }

    private void saveToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful())
                        {
                            userRef.child(currentUserID).child("token")
                                    .setValue(task.getResult().getToken())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                             if(task.isSuccessful())
                                             {
                                                 fetchDataFromDatabase();
                                             }
                                        }
                                    });
                        }
                    }
                });
    }


    private void fetchDataFromDatabase()
    {
        total=0;
        solved=0;
        unsolved=0;
        userRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Glide.with(Home.this).load(dataSnapshot.child("url").getValue().toString())
                        .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(profile);

                name.setText(dataSnapshot.child("fname").getValue().toString()+" "+
                        dataSnapshot.child("lname").getValue().toString());
                email.setText(dataSnapshot.child("email").getValue().toString());
                state.setText(dataSnapshot.child("state").getValue().toString());

                if(dataSnapshot.child("complaints").exists())
                {
                    for(DataSnapshot dataSnapshot1:dataSnapshot.child("complaints").getChildren())
                    {
                        if(dataSnapshot1.child("status").getValue().toString().equals("Complaint Solved"))
                        {
                            solved += 1;
                            tsc.setText(String.valueOf(solved));
                        }
                        else
                        {
                            unsolved+=1;
                            tusc.setText(String.valueOf(unsolved));
                        }

                        total += 1;
                        tc.setText(String.valueOf(total));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id) {

            case R.id.home:
                startActivity(new Intent(Home.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                startActivity(new Intent(Home.this,Home.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(Home.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(Home.this,Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status :
                startActivity(new Intent(Home.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint :
                startActivity(new Intent(Home.this,ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,Home.this,firebaseAuth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(Home.this,ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(Home.this,ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(Home.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(Home.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    @Override
    public void onBackPressed()
    {
        super.alert(R.layout.logout_alert,Home.this,firebaseAuth);
    }
}
