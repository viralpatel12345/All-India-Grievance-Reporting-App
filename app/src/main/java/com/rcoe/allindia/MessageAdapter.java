package com.rcoe.allindia;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
     private List<Messages> userMessagesList;
     private FirebaseAuth auth;
     private String messageReceiverId,messageReceiverName;

     public MessageAdapter(List<Messages> userMessagesList,String messageReceiverId,String messageReceiverName)
     {
         this.userMessagesList    = userMessagesList;
         this.messageReceiverId   = messageReceiverId;
         this.messageReceiverName = messageReceiverName;
     }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
       View view = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.custom_messages_layout,parent,false);

       auth = FirebaseAuth.getInstance();

       return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
         String messageSenderId = auth.getCurrentUser().getUid();
         final Messages messages = userMessagesList.get(position);
         String fromUserID = messages.getFrom();
         String fromMessageType = messages.getType();

         if(fromMessageType.equals("text"))
         {
             holder.receiverMessageText.setVisibility(View.INVISIBLE);
             holder.senderMessageText.setVisibility(View.INVISIBLE);
             holder.senderTime.setVisibility(View.INVISIBLE);
             holder.receiverTime.setVisibility(View.INVISIBLE);

             if(messageSenderId.equals(fromUserID))
             {
                 holder.senderMessageText.setVisibility(View.VISIBLE);
                 holder.senderTime.setVisibility(View.VISIBLE);
                 holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                 holder.senderMessageText.setText(messages.getMessage());
                 holder.senderTime.setText(messages.getTime());
             }
             else
             {
                 holder.receiverMessageText.setVisibility(View.VISIBLE);
                 holder.receiverTime.setVisibility(View.VISIBLE);
                 holder.receiverMessageText.setBackgroundResource(R.drawable.border);
                 holder.receiverMessageText.setText(messages.getMessage());
                 holder.receiverTime.setText(messages.getTime());
             }
         }

         if(fromUserID.equals(messageSenderId))
         {
             holder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v)
                 {
                     final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                     LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
                     View view = inflater.inflate(R.layout.chat_activity_alertbox, null);
                     builder.setView(view);
                     final AlertDialog alertDialog = builder.create();
                     view.findViewById(R.id.delete_message).setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             deleteSentMessages(position,holder);
                             alertDialog.dismiss();

                         }
                     });
                     view.findViewById(R.id.copy_message).setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             ClipboardManager clipboard = (ClipboardManager) holder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                             ClipData clip = ClipData.newPlainText("COPY", messages.getMessage());
                             clipboard.setPrimaryClip(clip);
                             Toast.makeText(holder.itemView.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
                             alertDialog.dismiss();
                         }
                     });

                     alertDialog.show();
                 }
             });
         }
         else if(!fromUserID.equals(messageSenderId))
         {
             holder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v)
                 {
                     final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                     LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
                     View view = inflater.inflate(R.layout.chat_activity_alertbox, null);
                     builder.setView(view);
                     final AlertDialog alertDialog = builder.create();
                     view.findViewById(R.id.delete_message).setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             deleteReceivedMessages(position,holder);
                             alertDialog.dismiss();
                         }
                     });
                     view.findViewById(R.id.copy_message).setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             ClipboardManager clipboard = (ClipboardManager) holder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                             ClipData clip = ClipData.newPlainText("COPY", messages.getMessage());
                             clipboard.setPrimaryClip(clip);
                             Toast.makeText(holder.itemView.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
                             alertDialog.dismiss();
                         }
                     });

                     alertDialog.show();
                 }
             });
         }

    }

    private void deleteSentMessages(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
               .child(userMessagesList.get(position).getFrom())
               .child(userMessagesList.get(position).getTo())
               .child(userMessagesList.get(position).getMessageID())
               .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful())
                 {
                     Toast.makeText(holder.itemView.getContext(),"Deleted Successfully",Toast.LENGTH_LONG).show();
                     Intent intent = new Intent(holder.itemView.getContext(),MessageActivity.class);
                     intent.putExtra("receiver_user_id",messageReceiverId);
                     intent.putExtra("receiver_user_name",messageReceiverName);
                     holder.itemView.getContext().startActivity(intent);
                 }
                 else
                 {
                     Toast.makeText(holder.itemView.getContext(),"Error Occured",Toast.LENGTH_LONG).show();
                 }
            }
        });

    }

    private void deleteReceivedMessages(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"Deleted Successfully",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(holder.itemView.getContext(),MessageActivity.class);
                    intent.putExtra("receiver_user_id",messageReceiverId);
                    intent.putExtra("receiver_user_name",messageReceiverName);
                    holder.itemView.getContext().startActivity(intent);
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occured",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
     {

         public TextView senderMessageText,receiverMessageText,senderTime,receiverTime;

         public MessageViewHolder(@NonNull View itemView)
         {
             super(itemView);

             senderMessageText   = itemView.findViewById(R.id.sender_message_text);
             receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
             senderTime          = itemView.findViewById(R.id.sender_message_time);
             receiverTime        = itemView.findViewById(R.id.receiver_message_time);
         }
     }

}