package com.rcoe.allindia;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends extends BaseActivity {

    private ConstraintLayout constraintLayout;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String receiverId = null, current_state;
    private String senderId;
    int flag = 0;
    private FirebaseUser user;
    private ImageView findButton;
    private CircleImageView circleImageView;
    private Button notifyButton, cancelRequestButton;
    private EditText mobile;
    private TextView name, email;
    private ProgressBar progressBar;
    private DatabaseReference userReference, chatReqRef, contactsRef;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        getSupportActionBar().setTitle("FIND FRIENDS");

        user = auth.getCurrentUser();
        senderId = user.getUid();
        constraintLayout = findViewById(R.id.constraintLayout);
        mobile = findViewById(R.id.mobile);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        progressBar = findViewById(R.id.progressBar);
        findButton = findViewById(R.id.findButton);
        notifyButton = findViewById(R.id.notifyButton);
        cancelRequestButton = findViewById(R.id.cancelRequestButton);
        circleImageView = findViewById(R.id.profile);
        current_state = "new";
        userReference = FirebaseDatabase.getInstance().getReference().child("user");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(), 0);

                if (isConnected(FindFriends.this)) {
                    flag = 0;
                    progressBar.setVisibility(View.VISIBLE);
                    final String number = mobile.getText().toString();
                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (number.equals(dataSnapshot1.child("mobile").getValue(String.class))) {
                                    flag = 1;
                                    name.setVisibility(View.VISIBLE);
                                    email.setVisibility(View.VISIBLE);
                                    receiverId = dataSnapshot1.getKey();
                                    Glide.with(FindFriends.this).load(dataSnapshot1.child("url").getValue().toString())
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
                                    }).into(circleImageView);

                                    email.setText(dataSnapshot1.child("email").getValue(String.class));
                                    name.setText(dataSnapshot1.child("fname").getValue(String.class) + " " + dataSnapshot1.child("lname").getValue(String.class));
                                    notifyButton.setVisibility(View.VISIBLE);
                                    manageChatRequests();
                                    break;
                                }

                            }
                            progressBar.setVisibility(View.GONE);
                            if (flag == 0) {
                                notifyButton.setVisibility(View.GONE);
                                circleImageView.setImageBitmap(null);
                                name.setVisibility(View.GONE);
                                email.setVisibility(View.GONE);
                                Toast.makeText(FindFriends.this, "Friend not found", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(FindFriends.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void manageChatRequests() {
        chatReqRef.child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverId)) {
                    String request_type = dataSnapshot.child(receiverId).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        current_state = "request_sent";
                        notifyButton.setText("Cancel Friend Request");
                    } else if (request_type.equals("received")) {
                        current_state = "request_received";
                        notifyButton.setText("Accept Friend Request");

                        cancelRequestButton.setVisibility(View.VISIBLE);
                        cancelRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isConnected(FindFriends.this)) {
                                    cancelChatRequest();
                                } else {
                                    Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
                else {
                    current_state="new";
                    notifyButton.setText("Send Friend Request");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!receiverId.equals(senderId)) {
            notifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (current_state.equals("new")) {
                        if (isConnected(FindFriends.this)) {
                            sendChatRequest();
                        } else {
                            Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                        }

                    }
                    if (current_state.equals("request_sent")) {
                        if (isConnected(FindFriends.this)) {
                            cancelChatRequest();
                        } else {
                            Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (current_state.equals("request_received")) {
                        if (isConnected(FindFriends.this)) {
                            acceptChatRequest();
                        } else {
                            Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (current_state.equals("friends")) {
                        if (isConnected(FindFriends.this)) {
                            removeThisContact();
                        } else {
                            Toast.makeText(FindFriends.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            notifyButton.setVisibility(View.GONE);
        }
    }

    private void removeThisContact() {
        notifyButton.setEnabled(false);
        cancelRequestButton.setEnabled(false);
        contactsRef.child(senderId).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                notifyButton.setEnabled(true);
                                                current_state = "new";
                                                notifyButton.setText("Send Friend Request");
                                                cancelRequestButton.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        notifyButton.setEnabled(false);
        cancelRequestButton.setEnabled(false);
        contactsRef.child(senderId).child(receiverId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverId).child(senderId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatReqRef.child(senderId).child(receiverId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    chatReqRef.child(receiverId).child(senderId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        current_state = "friends";
                                                                                        notifyButton.setEnabled(true);
                                                                                        notifyButton.setText("Remove this Contact");
                                                                                        cancelRequestButton.setVisibility(View.GONE);

                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void cancelChatRequest() {
        notifyButton.setEnabled(false);
        cancelRequestButton.setEnabled(false);
        chatReqRef.child(senderId).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatReqRef.child(receiverId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                notifyButton.setEnabled(true);
                                                current_state = "new";
                                                notifyButton.setText("Send Friend Request");
                                                cancelRequestButton.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        notifyButton.setEnabled(false);
        chatReqRef.child(senderId).child(receiverId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatReqRef.child(receiverId).child(senderId)
                            .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                notifyButton.setEnabled(true);
                                current_state = "request_sent";
                                notifyButton.setText("Cancel Friend Request");
                                sendNotification();
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendNotification()
    {
        userReference.child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("token").exists())
                {
                    final String tk = dataSnapshot.child("token").getValue().toString();
                    userReference.child(senderId).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            String name = dataSnapshot1.child("fname").getValue().toString() + " "
                                    + dataSnapshot1.child("lname").getValue().toString();

                            JSONObject mainObj = new JSONObject();
                            try
                            {
                                mainObj.put("to", tk);
                                JSONObject notificationObj = new JSONObject();
                                notificationObj.put("title", "New Friend Request");
                                notificationObj.put("body", "New friend request from " + name);

                                mainObj.put("notification", notificationObj);

                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                                        mainObj,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
//                                    Toast.makeText(FindFriends.this,response.toString(),Toast.LENGTH_LONG).show();
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(FindFriends.this, error.getMessage(), Toast.LENGTH_LONG).show();

                                    }
                                }
                                ){
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> header = new HashMap<>();
                                        header.put("content-type", "application/json");
                                        header.put("authorization", "key=AAAACj7eXPo:APA91bHrppecuwt4dbhFzWnS5Ts2uIsWg4IayQC5V9_ziLqLgP3so7ZGp578QzJ5XjYjTiG-y-i_ewEupMTT9eamABY_n7U3dFKamF_vqedhLvCuivureufVEvZKb2ezfteJmzTloMW9");
                                        return header;
                                    }
                                };

                                MySingleton.getInstance(FindFriends.this).addToQueue(request);

                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

        switch (id) {

            case R.id.home:
                startActivity(new Intent(FindFriends.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh:
                startActivity(new Intent(FindFriends.this, FindFriends.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(FindFriends.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile:
                startActivity(new Intent(FindFriends.this, Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status:
                startActivity(new Intent(FindFriends.this, ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint:
                startActivity(new Intent(FindFriends.this, ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert, FindFriends.this, auth);
                break;

            case R.id.share:
                share();
                break;

            case R.id.change_password:
                startActivity(new Intent(FindFriends.this, ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats:
                startActivity(new Intent(FindFriends.this, ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends:
                startActivity(new Intent(FindFriends.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests:
                startActivity(new Intent(FindFriends.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(FindFriends.this, ChatActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}