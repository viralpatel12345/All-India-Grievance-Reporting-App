package com.rcoe.allindia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkerMyAdapter extends RecyclerView.Adapter<WorkerMyAdapter.MyViewHolder>
{
    Context context;
    ArrayList<UserAndComplaint> userAndComplaints;

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView complaint,sub_complaint,date,email;
        CircleImageView img;
        ProgressBar progressBar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            complaint = itemView.findViewById(R.id.complaint);
            sub_complaint = itemView.findViewById(R.id.sub_complaint);
            date = itemView.findViewById(R.id.dateofcomplaint);
            img = itemView.findViewById(R.id.complaint_image);
            email = itemView.findViewById(R.id.email_id);
            progressBar = itemView.findViewById(R.id.pb);
        }
    }


    public WorkerMyAdapter(Context context,ArrayList<UserAndComplaint> userAndComplaints)
    {
        this.context =context;
        this.userAndComplaints = userAndComplaints;

    }
    @NonNull
    @Override
    public WorkerMyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkerMyAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.worker_display_complaint_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
    {

        Glide.with(context).load(userAndComplaints.get(position).getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
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
                }).into(holder.img);

        holder.complaint.setText(userAndComplaints.get(position).getComplaint_type());
        holder.sub_complaint.setText(userAndComplaints.get(position).getComplaint_sub_type());
        holder.email.setText(userAndComplaints.get(position).getEmail_address());
        holder.date.setText(userAndComplaints.get(position).getDateofcomplaint());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context,WorkerComplaintDescription.class);
                intent.putExtra("postal_code",userAndComplaints.get(position).getPostalcode());
                intent.putExtra("userId",userAndComplaints.get(position).getUser_id());
                intent.putExtra("complaintId",userAndComplaints.get(position).getComplaint_id());
                intent.putExtra("status",userAndComplaints.get(position).getStatus());
                context.startActivity(intent);
                ((Activity) holder.itemView.getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userAndComplaints.size();
    }
}
