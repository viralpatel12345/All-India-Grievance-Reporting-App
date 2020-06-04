package com.rcoe.allindia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionPropagation;
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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Complaints> complaints;

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView complaint,sub_complaint,date;
        CircleImageView img;
        ProgressBar progressBar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            complaint = itemView.findViewById(R.id.complaint);
            sub_complaint = itemView.findViewById(R.id.sub_complaint);
            date = itemView.findViewById(R.id.dateofcomplaint);
            img = itemView.findViewById(R.id.complaint_image);
            progressBar = itemView.findViewById(R.id.pb);
        }
    }


    public MyAdapter(Context context,ArrayList<Complaints> complaints)
    {
        this.context =context;
        this.complaints = complaints;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.list_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position)
    {
            Glide.with(context).load(complaints.get(position).getUrl())
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

            holder.complaint.setText(complaints.get(position).getC_type());
            holder.sub_complaint.setText(complaints.get(position).getC_subtype());
            holder.date.setText(complaints.get(position).getDateofcomplaint());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,ComplaintDescription.class);
                    intent.putExtra("complaintId",complaints.get(position).getKey());
                    context.startActivity(intent);
                    ((Activity) holder.itemView.getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            });

    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

}
