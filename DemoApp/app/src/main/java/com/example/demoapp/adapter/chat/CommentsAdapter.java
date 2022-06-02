package com.example.demoapp.adapter.chat;


import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {
    Context context;
    List<Comment> commentList;

    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // getData
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        // convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy, HH:mm aa", calendar).toString();

        // set the data
        holder.tvName.setText(name);
        holder.tvComments.setText(comment);
        holder.tvTime.setText(pTime);
        // set user dp
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_face).into(holder.avatarIv);
        }catch (Exception e){

        }


    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        // declare views from row_comments.xml
        ImageView avatarIv;
        TextView tvName, tvComments, tvTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name_comment);
            tvComments = itemView.findViewById(R.id.tv_comment);
            tvTime = itemView.findViewById(R.id.tv_time_comment);

        }
    }
}
