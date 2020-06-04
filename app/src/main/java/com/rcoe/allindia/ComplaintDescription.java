package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ComplaintDescription extends BaseActivity {

    private String cid;
    private TextView complaintId,complaintType,subComplaintType,locationCoordinates,
                     complaintStatus,complaintDescription,area,complaintDate;
    private Button openGoogleMap;
    private CircleImageView circleImageView;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private DatabaseReference reference;
    private ProgressBar progressBar;
    private String latitude,longitude;
    private ImageView copyComplaintId;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_description);

        cid = getIntent().getStringExtra("complaintId");
        getSupportActionBar().setTitle("COMPLAINT DESCRIPTION");

        circleImageView      = findViewById(R.id.complaint_imageView);
        complaintStatus      = findViewById(R.id.comp_status);
        complaintId          = findViewById(R.id.complaint_id);
        complaintType        = findViewById(R.id.complaint_type);
        subComplaintType     = findViewById(R.id.complaint_sub_type);
        complaintDescription = findViewById(R.id.complaint_description);
        locationCoordinates  = findViewById(R.id.location_coordinates);
        openGoogleMap        = findViewById(R.id.openGoogleMap);
        area                 = findViewById(R.id.area);
        progressBar          = findViewById(R.id.progress);
        complaintDate        = findViewById(R.id.complaint_date);
        copyComplaintId      = findViewById(R.id.copy_complaint_id);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("user").child(userId)
                                                  .child("complaints").child(cid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String complaint_status = dataSnapshot.child("status").getValue().toString().trim();
                if(complaint_status.equals("Complaint Solved"))
                {
                    url = dataSnapshot.child("solved_url").getValue().toString();
                    complaintStatus.setTextColor(Color.rgb(56,104,30));
                }
                else
                {
                    url = dataSnapshot.child("url").getValue().toString();
                }
                complaintStatus.setText(complaint_status);
                complaintId.setText(cid);
                complaintType.setText(dataSnapshot.child("c_type").getValue().toString());
                subComplaintType.setText(dataSnapshot.child("c_subtype").getValue().toString());
                area.setText(dataSnapshot.child("state").getValue().toString().trim());
                complaintDescription.setText(dataSnapshot.child("description").getValue().toString());
                complaintDate.setText(dataSnapshot.child("dateofcomplaint").getValue().toString());
                latitude=dataSnapshot.child("lantitude").getValue().toString();
                longitude=dataSnapshot.child("longitude").getValue().toString();
                locationCoordinates.setText(latitude+" , "+longitude);
                Glide.with(ComplaintDescription.this).load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        progressBar.setVisibility(View.GONE);

        openGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/place/"+latitude+","+longitude));
                startActivity(intent);
            }
        });

        copyComplaintId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(ComplaintDescription.this,cid);
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
