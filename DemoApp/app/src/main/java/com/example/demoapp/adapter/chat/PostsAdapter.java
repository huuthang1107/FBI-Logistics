package com.example.demoapp.adapter.chat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.model.Post;
import com.example.demoapp.view.activity.chat.AddPostActivity;
import com.example.demoapp.view.activity.chat.DetailProfileActivity;
import com.example.demoapp.view.activity.chat.PostDetailActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    private Context context;
    private List<Post> postList;
    private String myUid;

    private DatabaseReference likesRef; // for likes database node
    private DatabaseReference postRef; // reference of post

    boolean mProcessLike = false;

    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout row_post
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        // get data
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();
        // set likes for each post
        setLikes(holder, pId);

        // convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy, HH:mm aa", calendar).toString();


        // set data
        holder.tvUName.setText(uName);
        holder.tvPTime.setText(pTime);
        holder.tvPTitle.setText(pTitle);
        holder.tvPDescription.setText(pDescription);
        holder.tvPLikes.setText(pLikes + " Likes");
        holder.tvPComments.setText(pComments + " Comments");
        // set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face).into(holder.uPictureIv);
        } catch (Exception e) {

        }

        // set post image
        // if there is no image i.e pImage.quals("noImage") the hide ImageView
        if (pImage.equals("noImage")) {
            // hide imageview
            holder.pImageIv.setVisibility(View.GONE);
        } else {
            // show imageview
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }


        // handle button clicks
        holder.btnmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // will implement later
                showMoreOptions(holder.btnmore, uid, myUid, pId, pImage);
            }
        });
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get total number of like for the post, whose like button clicked
                // if currently signed in user has not liked it before
                // increase value by 1, otherwise decrease value by 1
                int plikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                // get id of the post clicked
                String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike) {
                            if (snapshot.child(postIde).hasChild(myUid)) {
                                // already liked, so remove like
                                postRef.child(postIde).child("pLikes").setValue("" + (plikes - 1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {
                                // not liked, like it
                                postRef.child(postIde).child("pLikes").setValue("" + (plikes + 1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); // will get datail of post using this id, its id of the post clicked
                context.startActivity(intent);

            }
        });
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*
              some posts contains only text, and some contains image and text so,
              we will handle them both
               */
                // get image from imageview
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.pImageIv.getDrawable();
                if(bitmapDrawable == null){
                    // post without image
                    shareTextOnly(pTitle, pDescription);
                }
                else {
                    //post with image


                    //convert image to bimap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* click to go there profile activity with uid, this uid is of clicked user
                    which will be used to show user specific data/posts
                */
                Intent intent = new Intent(context, DetailProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);

            }
        });

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
    }

    private void shareTextOnly(String title, String description) {
        // concatenate title and description to share
        String shareBody = title + "\n" + description;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));
    }

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    // user has liked this post
                    holder.btnLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.btnLike.setText("Liked");
                }else{
                    // user has not liked this post
                    holder.btnLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.btnLike.setText("Liked");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton btnmore, String uid, String myUid, final String pId, final String pImage) {
        // creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, btnmore, Gravity.END);

        // show delete option in only post(s) of currently signed in user

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");



        // item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    // edit is click
                    // start AddPostActivity with key "editpost" and the id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }else if(id == 2){
                    // start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); // will get datail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                }

                return false;
            }
        });
        // show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        // post can be with or without image
        if (pImage.equals("noImage")) {
            // post is without image
            deleteWithoutImage(pId);

        } else {
            // post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        // progress bar
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        /*
            1. Delete Image using url
            2. Delete from database using post id
         */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // image deleted, now delete database
                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches

                                }
                                // deleted
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed, can't go further
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        // progress bar
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue(); // remove values from firebase where pid matches

                }
                // deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        // views from row_post
        ImageView uPictureIv, pImageIv;
        TextView tvUName, tvPTime, tvPTitle, tvPDescription, tvPLikes, tvPComments;
        ImageButton btnmore;
        Button btnLike, btnComment, btnShare;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            tvUName = itemView.findViewById(R.id.tv_uName);
            tvPTime = itemView.findViewById(R.id.tv_pTime);
            tvPTitle = itemView.findViewById(R.id.tv_pTitle);
            tvPDescription = itemView.findViewById(R.id.tv_pDescription);
            tvPLikes = itemView.findViewById(R.id.tv_pLikes);
            tvPComments = itemView.findViewById(R.id.tv_pComments);
            btnmore = itemView.findViewById(R.id.btn_more);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            btnShare = itemView.findViewById(R.id.btn_share);
            profileLayout = itemView.findViewById(R.id.profile_layout);

        }
    }
}
