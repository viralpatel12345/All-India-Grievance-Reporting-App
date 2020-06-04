package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class WorkerComplaintDescription extends BaseActivity {

    private String postal_code,user_id,complaint_id,status,worker_id,latitude,longitude,imageRef = null,
            from_search = null;
    private TextView username,email,state,mobile,complaintId,complaintType,subComplaintType,locationCoordinates,
            complaintStatus,complaintDescription,area,complaintDate,solvedComplaintHeading;
    private Button openGoogleMap,solveThisComplaint;
    private Uri imageUri = null;
    private ProgressBar progressBar;
    private ImageView complaintImage,copyComplaintId,solvedComplaintImage,copyEmail,copyMobile;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userReference,complaintReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_complaint_description);

        if(getIntent().hasExtra("from_search"))
        {
            from_search = getIntent().getStringExtra("from_search");
        }

        postal_code  = getIntent().getStringExtra("postal_code");
        user_id      = getIntent().getStringExtra("userId");
        complaint_id = getIntent().getStringExtra("complaintId");
        status       = getIntent().getStringExtra("status");

        getSupportActionBar().setTitle("COMPLAINT DESCRIPTION");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        worker_id    = firebaseUser.getUid();

        complaintImage         = findViewById(R.id.complaint_imageView);
        solveThisComplaint     = findViewById(R.id.solveComplaintButton);
        complaintStatus        = findViewById(R.id.comp_status);
        complaintId            = findViewById(R.id.complaint_id);
        complaintType          = findViewById(R.id.complaint_type);
        subComplaintType       = findViewById(R.id.complaint_sub_type);
        complaintDescription   = findViewById(R.id.complaint_description);
        locationCoordinates    = findViewById(R.id.location_coordinates);
        openGoogleMap          = findViewById(R.id.openGoogleMap);
        copyEmail              = findViewById(R.id.copy_useremail);
        copyMobile             = findViewById(R.id.copy_usermobile);
        area                   = findViewById(R.id.area);
        username               = findViewById(R.id.username);
        email                  = findViewById(R.id.useremail);
        state                  = findViewById(R.id.userstate);
        mobile                 = findViewById(R.id.usermobile);
        progressBar            = findViewById(R.id.progress);
        solvedComplaintImage   = findViewById(R.id.solved_complaint_imageView);
        complaintDate          = findViewById(R.id.complaint_date);
        copyComplaintId        = findViewById(R.id.copy_complaint_id);
        solvedComplaintHeading = findViewById(R.id.solve_complaint_heading);
        progressDialog         = new ProgressDialog(WorkerComplaintDescription.this);
        progressDialog.setMessage("Solving");

        userReference = FirebaseDatabase.getInstance().getReference().child("user")
                .child(user_id);

        complaintReference = FirebaseDatabase.getInstance().getReference().child("user")
                .child(user_id).child("complaints").child(complaint_id);

        loadData();

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
                copyToClipboard(WorkerComplaintDescription.this,complaint_id);
            }
        });

        copyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(WorkerComplaintDescription.this,email.getText().toString());
            }
        });

        copyMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(WorkerComplaintDescription.this,mobile.getText().toString());
            }
        });

        solvedComplaintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });

        solveThisComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isConnected(WorkerComplaintDescription.this))
                {
                    if(imageUri!=null)
                    {
                          solve_complaint();
                    }
                    else
                    {
                        Toast.makeText(WorkerComplaintDescription.this,"SOLVED COMPLAINT IMAGE REQUIRED",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(WorkerComplaintDescription.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
                }

            }
        });

        mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+mobile.getText().toString()));//change the number
                startActivity(callIntent);
            }
        });

    }

    private void solve_complaint()
    {
        progressDialog.show();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        final StorageReference childImageRef = storageReference.child(user_id).child("complaints").child(complaint_id).child("solved_complaint");
        final UploadTask uploadUserProfile = childImageRef.putFile(imageUri);

        uploadUserProfile.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> downloadUrl = childImageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageRef = uri.toString();
                        change_complaint_status();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WorkerComplaintDescription.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    imageUri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    solvedComplaintImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(WorkerComplaintDescription.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(WorkerComplaintDescription.this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void crop() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(WorkerComplaintDescription.this);
    }


    private void change_complaint_status()
    {
        complaintReference.child("status").setValue("Complaint Partially Solved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            complaintReference.child("workerId").setValue(worker_id)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            complaintReference.child("solved_url").setValue(imageRef)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        solvedComplaintImage.setEnabled(false);
                                                        solveThisComplaint.setEnabled(false);
                                                        success_alert();
                                                    }
                                                }
                                            });

                                        }
                                    });
                        }
                    }
                });
    }

    private void loadData()
    {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                username.setText(dataSnapshot.child("fname").getValue().toString()+" "+dataSnapshot.child("lname").getValue().toString());
                email.setText(dataSnapshot.child("email").getValue().toString());
                mobile.setText(dataSnapshot.child("mobile").getValue().toString());
                state.setText(dataSnapshot.child("state").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        complaintReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String complaint_status = dataSnapshot.child("status").getValue().toString().trim();
                if(complaint_status.equals("Complaint Solved"))
                {
                    complaintStatus.setTextColor(Color.rgb(56,104,30));
                    solvedComplaintImage.setBackground(null);
                    solvedComplaintImage.setVisibility(View.VISIBLE);
                    solvedComplaintImage.setEnabled(false);
                    solveThisComplaint.setVisibility(View.GONE);
                    solvedComplaintHeading.setText("Complaint has been resolved");

                    Glide.with(WorkerComplaintDescription.this).load(dataSnapshot.child("solved_url").getValue().toString())
                            .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(solvedComplaintImage);

                }

                solvedComplaintImage.setVisibility(View.VISIBLE);
                complaintStatus.setText(complaint_status);
                complaintId.setText(complaint_id);
                complaintType.setText(dataSnapshot.child("c_type").getValue().toString());
                subComplaintType.setText(dataSnapshot.child("c_subtype").getValue().toString());
                area.setText(dataSnapshot.child("state").getValue().toString().trim());
                complaintDescription.setText(dataSnapshot.child("description").getValue().toString());
                complaintDate.setText(dataSnapshot.child("dateofcomplaint").getValue().toString());
                latitude=dataSnapshot.child("lantitude").getValue().toString();
                longitude=dataSnapshot.child("longitude").getValue().toString();
                locationCoordinates.setText(latitude+" , "+longitude);
                Glide.with(WorkerComplaintDescription.this).load(dataSnapshot.child("url").getValue().toString())
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
                        }).into(complaintImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    void success_alert()
    {
        progressDialog.dismiss();
        LayoutInflater inflater = LayoutInflater.from(WorkerComplaintDescription.this);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert, null);
        TextView tv   = view.findViewById(R.id.message);
        final Button copyId = view.findViewById(R.id.copy_btn);
        copyId.setVisibility(View.VISIBLE);
        copyId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(WorkerComplaintDescription.this,complaint_id);
            }
        });

        tv.setText("Complaint successfully solved\n\n" + complaint_id);
        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerComplaintDescription.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyId.setVisibility(View.GONE);

                if(from_search!=null)
                {
                    Intent intent = new Intent(WorkerComplaintDescription.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                }
                else
                {
                    Intent intent = new Intent(WorkerComplaintDescription.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status",status);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                }
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}