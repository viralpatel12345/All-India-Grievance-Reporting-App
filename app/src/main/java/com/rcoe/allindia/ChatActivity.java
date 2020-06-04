package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String currentUserId;
    private RecyclerView myContactList;
    private DividerItemDecoration mDividerItemDecoration;
    private DatabaseReference contactsRef,usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setTitle("CONTACT LIST");

        auth          = FirebaseAuth.getInstance();
        user          = auth.getCurrentUser();
        currentUserId = user.getUid();
        contactsRef   = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef      = FirebaseDatabase.getInstance().getReference().child("user");

        myContactList = findViewById(R.id.recyclerView);
        myContactList.setLayoutManager(new LinearLayoutManager(this));
        mDividerItemDecoration = new DividerItemDecoration(myContactList.getContext(),
                DividerItemDecoration.VERTICAL);

    }

    @Override
    protected void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                       View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.contact_list_layout,parent,false);
                       return new ContactsViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
                    {
                          final String userId = getRef(position).getKey();
                          usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                              {
                                  myContactList.addItemDecoration(mDividerItemDecoration);
                                  final String imgURL     = dataSnapshot.child("url").getValue().toString();
                                  final String fullName   = dataSnapshot.child("fname").getValue().toString().toUpperCase()+" "+
                                                             dataSnapshot.child("lname").getValue().toString().toUpperCase();
                                  final String userEmail  = dataSnapshot.child("email").getValue().toString();

                                  Glide.with(ChatActivity.this).load(imgURL)
                                          .diskCacheStrategy(DiskCacheStrategy.ALL)
                                          .centerCrop().listener(new RequestListener<Drawable>() {
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

                                  holder.username.setText(fullName);
                                  holder.email.setText(userEmail);

                                  holder.itemView.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          Intent chatIntent = new Intent(ChatActivity.this,MessageActivity.class);
                                          chatIntent.putExtra("receiver_user_id",userId);
                                          chatIntent.putExtra("receiver_user_name",fullName);
                                          startActivity(chatIntent);
                                          overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                          finish();
                                      }
                                  });
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {

                              }
                          });
                    }
                };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,email;
        CircleImageView profileImage;
        ProgressBar progressBar;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            username = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            profileImage = itemView.findViewById(R.id.profileImage);
            progressBar = itemView.findViewById(R.id.pb);
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
                startActivity(new Intent(ChatActivity.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                startActivity(new Intent(ChatActivity.this,ChatActivity.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(ChatActivity.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(ChatActivity.this,Profile.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.complaint_status :
                startActivity(new Intent(ChatActivity.this,ComplaintStatus.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.search_complaint :
                startActivity(new Intent(ChatActivity.this,ComplaintSearch.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,ChatActivity.this,auth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(ChatActivity.this,ChangePassword.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.chats :
                startActivity(new Intent(ChatActivity.this,ChatActivity.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.find_friends :
                startActivity(new Intent(ChatActivity.this, FindFriends.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.received_requests :
                startActivity(new Intent(ChatActivity.this, Requests.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ChatActivity.this, Home.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
