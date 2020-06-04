package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class Requests extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String currentUserId;
    private RecyclerView myRequestsList;
    private DatabaseReference chatRequestsRef,userRef,contactsRef;
    private DividerItemDecoration mDividerItemDecoration;
    private ProgressDialog progressDialog;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        getSupportActionBar().setTitle("REQUESTS");
        progressDialog = new ProgressDialog(Requests.this);
        progressDialog.setMessage("Processing");
        progressDialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        currentUserId     = user.getUid();
        myRequestsList = findViewById(R.id.recyclerView);
        myRequestsList.setLayoutManager(new LinearLayoutManager(this));
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        userRef         = FirebaseDatabase.getInstance().getReference().child("user");
        contactsRef     = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mDividerItemDecoration = new DividerItemDecoration(myRequestsList.getContext(),
                DividerItemDecoration.VERTICAL);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position, @NonNull Contacts model)
                    {
                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                 if(dataSnapshot.exists())
                                 {
                                     String type = dataSnapshot.getValue().toString();
                                     if(type.equals("received"))
                                     {
                                         myRequestsList.addItemDecoration(mDividerItemDecoration);
                                         userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                               @Override
                                               public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                               {
                                                    final String requestUsername = dataSnapshot.child("fname").getValue().toString()
                                                            +" "+dataSnapshot.child("lname").getValue().toString();

                                                    final String requestEmail = dataSnapshot.child("email").getValue().toString();
                                                    final String requestUrl   = dataSnapshot.child("url").getValue().toString();

                                                    holder.profileImage.setVisibility(View.VISIBLE);

                                                    holder.username.setVisibility(View.VISIBLE);
                                                    holder.email.setVisibility(View.VISIBLE);
                                                    holder.username.setText(requestUsername);
                                                    holder.email.setText(requestEmail);
                                                    Glide.with(Requests.this).load(requestUrl)
                                                           .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                           .centerCrop()
                                                           .listener(new RequestListener<Drawable>() {
                                                               @Override
                                                               public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                                   holder.progressBar.setVisibility(View.GONE);
                                                                   return false;
                                                               }

                                                               @Override
                                                               public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                                   holder.progressBar.setVisibility(View.GONE);
                                                                   return false;
                                                               }
                                                           }).into(holder.profileImage);
                                                    holder.acceptButton.setVisibility(View.VISIBLE);
                                                    holder.cancelButton.setVisibility(View.VISIBLE);

                                                    holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                            if(isConnected(getApplicationContext()))
                                                            {
                                                                progressDialog.show();
                                                                holder.acceptButton.setEnabled(false);
                                                                holder.cancelButton.setEnabled(false);
                                                                contactsRef.child(currentUserId).child(list_user_id)
                                                                        .child("Contacts").setValue("Saved")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    contactsRef.child(list_user_id).child(currentUserId)
                                                                                            .child("Contacts").setValue("Saved")
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                                    .removeValue()
                                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            if (task.isSuccessful())
                                                                                                                                            {
                                                                                                                                                 progressDialog.dismiss();
                                                                                                                                                 sendNotification(list_user_id);
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
                                                            else
                                                            {
                                                                Toast.makeText(getApplicationContext(), "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                                    holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                           if(isConnected(getApplicationContext()))
                                                           {
                                                               progressDialog.show();
                                                               holder.acceptButton.setEnabled(false);
                                                               holder.cancelButton.setEnabled(false);
                                                               chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                       .removeValue()
                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                               if (task.isSuccessful()) {
                                                                                   chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                           .removeValue()
                                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                               @Override
                                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                                   if (task.isSuccessful())
                                                                                                   {
                                                                                                       progressDialog.dismiss();
                                                                                                   }
                                                                                               }
                                                                                           });
                                                                               }
                                                                           }
                                                                       });
                                                           }
                                                           else
                                                           {
                                                               Toast.makeText(getApplicationContext(), "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                                                           }
                                                        }
                                                    });

                                               }

                                               @Override
                                               public void onCancelled(@NonNull DatabaseError databaseError)
                                               {

                                               }
                                           });
                                     }
                                 }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(Requests.this).inflate(R.layout.request_list_layout,parent,false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

            myRequestsList.setAdapter(adapter);
            adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,email;
        ProgressBar progressBar;
        CircleImageView profileImage;
        Button acceptButton , cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            username     = itemView.findViewById(R.id.name);
            email        = itemView.findViewById(R.id.email);
            progressBar  = itemView.findViewById(R.id.pb);
            profileImage = itemView.findViewById(R.id.profileImage);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            cancelButton = itemView.findViewById(R.id.cancelButton);

        }
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
                startActivity(new Intent(Requests.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                startActivity(new Intent(Requests.this,Requests.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(Requests.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status :
                startActivity(new Intent(Requests.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint :
                startActivity(new Intent(Requests.this,ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(Requests.this,Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,Requests.this,auth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(Requests.this,ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(Requests.this,ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(Requests.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(Requests.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    private void sendNotification(String list_user_id)
    {
        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("token").exists())
                {
                    final String tk = dataSnapshot.child("token").getValue().toString();
                    userRef.child(currentUserId).addValueEventListener(new ValueEventListener()
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
                                notificationObj.put("title", "Friend Request Accepted");
                                notificationObj.put("body", name + " accepted your friend request");

                                mainObj.put("notification", notificationObj);

                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                                        mainObj,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(Requests.this, error.getMessage(), Toast.LENGTH_LONG).show();

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

                                MySingleton.getInstance(Requests.this).addToQueue(request);

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
    public void onBackPressed()
    {
        startActivity(new Intent(Requests.this, ChatActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
