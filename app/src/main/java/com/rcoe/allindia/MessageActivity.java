package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends BaseActivity {

    private String messageReceiverId,messageReceiverName,messageSenderId;
    private ImageView sendMessageButton;
    private EditText messageInputText;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference rootReference,contactsRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private RecyclerView.SmoothScroller smoothScroller;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.setCancelable(false);

        messageReceiverId   = getIntent().getExtras().get("receiver_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("receiver_user_name").toString();
        getSupportActionBar().setTitle(messageReceiverName);

        auth                = FirebaseAuth.getInstance();
        user                = auth.getCurrentUser();
        messageSenderId     = user.getUid();
        sendMessageButton   = findViewById(R.id.sendMessageButton);
        messageInputText    = findViewById(R.id.editText);
        rootReference       = FirebaseDatabase.getInstance().getReference();
        contactsRef         = FirebaseDatabase.getInstance().getReference().child("Contacts");
        messageAdapter      = new MessageAdapter(messagesList,messageReceiverId,messageReceiverName);
        userMessagesList    = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isConnected(MessageActivity.this))
                {
                    sendMessage();
                }
                else
                {
                    Toast.makeText(MessageActivity.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagesList.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messagesList.clear();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        smoothScroller = new LinearSmoothScroller(MessageActivity.this) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        messagesList.clear();
        rootReference.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                            Messages messages = dataSnapshot.getValue(Messages.class);

                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();

                            smoothScroller.setTargetPosition(userMessagesList.getAdapter().getItemCount());
                            linearLayoutManager.startSmoothScroll(smoothScroller);

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String getCurrentDateAndTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm aa");
        Date date = new Date();
        return dateFormat.format(date).toUpperCase();
    }


    private void sendMessage()
    {
       final String message = messageInputText.getText().toString().trim();
       if(TextUtils.isEmpty(message))
       {
           Toast.makeText(MessageActivity.this,"first write your message",Toast.LENGTH_LONG).show();
       }
       else
       {
           String messageSenderRef   = "Messages/" + messageSenderId +"/" + messageReceiverId;
           String messageReceiverRef = "Messages/" + messageReceiverId +"/" + messageSenderId;

           DatabaseReference userMessageKeyRef = rootReference.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();
           String messagePushID = userMessageKeyRef.getKey();

           Map messageTextBody  = new HashMap();

           messageTextBody.put("message",message);
           messageTextBody.put("type","text");
           messageTextBody.put("from",messageSenderId);
           messageTextBody.put("to",messageReceiverId);
           messageTextBody.put("messageID",messagePushID);
           messageTextBody.put("time",getCurrentDateAndTime());

           Map messageBodyDetails = new HashMap();
           messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
           messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

           rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {

                    }
                    else
                    {
                        Toast.makeText(MessageActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
           });
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.home:
                startActivity(new Intent(MessageActivity.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh:
                Intent chatIntent = new Intent(MessageActivity.this, MessageActivity.class);
                chatIntent.putExtra("receiver_user_id",messageReceiverId);
                chatIntent.putExtra("receiver_user_name",messageReceiverName);
                startActivity(chatIntent);
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(MessageActivity.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile:
                startActivity(new Intent(MessageActivity.this, Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status:
                startActivity(new Intent(MessageActivity.this, ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint:
                startActivity(new Intent(MessageActivity.this, ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert, MessageActivity.this, auth);
                break;

            case R.id.clear_chat :
                clearChat();
                break;

            case R.id.remove_contact :
                alert();
                break;

            case R.id.share:
                share();
                break;

            case R.id.change_password:
                startActivity(new Intent(MessageActivity.this, ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats:
                startActivity(new Intent(MessageActivity.this, ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends:
                startActivity(new Intent(MessageActivity.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests:
                startActivity(new Intent(MessageActivity.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    void alert()
    {
        LayoutInflater inflater = LayoutInflater.from(MessageActivity.this);
        View view = inflater.inflate(R.layout.remove_contact_alert, null);
        TextView textView = view.findViewById(R.id.tv);
        textView.setText("REMOVE "+messageReceiverName+" ?\n\nYou will be also removed from receiver's contact list");
        final AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setView(view);
        builder.setCancelable(false);
        final CheckBox checkBox = view.findViewById(R.id.checkBox);
        final EditText password = view.findViewById(R.id.pw);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                progressDialog.setMessage("Authenticating");
                progressDialog.show();

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password.getText().toString().trim());
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                          if(task.isSuccessful())
                          {
                              if(!checkBox.isChecked())
                              {
                                  removeThisContact();
                              }
                              else if(checkBox.isChecked())
                              {
                                  removeThisContactWithMessages();
                              }
                          }
                          else
                          {
                              progressDialog.dismiss();
                              Toast.makeText(MessageActivity.this,"Incorrect Password",Toast.LENGTH_SHORT).show();
                          }
                    }
                });

            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }


    private void clearChat()
    {
        progressDialog.setMessage("Processing");
        progressDialog.show();
        rootReference.child("Messages")
                .child(messageSenderId)
                .child(messageReceiverId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Intent chatIntent = new Intent(MessageActivity.this, MessageActivity.class);
                    chatIntent.putExtra("receiver_user_id",messageReceiverId);
                    chatIntent.putExtra("receiver_user_name",messageReceiverName);
                    progressDialog.dismiss();
                    startActivity(chatIntent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                }
            }
        });
    }

    private void removeThisContact() {

        progressDialog.setMessage("Processing");
        contactsRef.child(messageSenderId).child(messageReceiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(messageReceiverId).child(messageSenderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(MessageActivity.this,ChatActivity.class));
                                                Toast.makeText(MessageActivity.this,"Contact Removed Successfully",Toast.LENGTH_LONG).show();
                                                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void removeThisContactWithMessages()
    {
        progressDialog.setMessage("Processing");
        contactsRef.child(messageSenderId).child(messageReceiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(messageReceiverId).child(messageSenderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                rootReference.child("Messages")
                                                        .child(messageSenderId)
                                                        .child(messageReceiverId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    rootReference.child("Messages")
                                                                            .child(messageReceiverId)
                                                                            .child(messageSenderId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    progressDialog.dismiss();
                                                                                    startActivity(new Intent(MessageActivity.this,ChatActivity.class));
                                                                                    Toast.makeText(MessageActivity.this,"Contact Removed Successfully",Toast.LENGTH_LONG).show();
                                                                                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                                                                                    finish();
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MessageActivity.this,ChatActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
