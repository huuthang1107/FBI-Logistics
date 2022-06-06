package com.example.demoapp.adapter.chat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.model.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {
    Context context;
    List<Comment> commentList;
    String myUid, postId;

    public CommentsAdapter(Context context, List<Comment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
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

        // comment click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if this comment is by currently signed in user or not
                if(myUid.equals(uid)){
                    // my comment
                    // show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // delete comment
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    // show dialog
                    builder.create().show();
                }else{
                    // not my comment
                    Toast.makeText(context, "Can't delete other's commert...", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void deleteComment(String cid) {
       final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); // it will delete the comment

        // now update the comments
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) -1;
                ref.child("pComments").setValue("" + newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
