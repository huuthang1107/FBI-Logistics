package com.example.demoapp.adapter.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.model.Users;
import com.example.demoapp.view.activity.chat.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyHolder> {
    Context context;
    List<Users> usersList; // get User info
    private HashMap<String, String> lastMessageMap;

    public ChatListAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // get data
        String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userName = usersList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.tvName.setText(userName);
        if(lastMessage== null || lastMessage.equals("default")){
            holder.tvLastMessage.setVisibility(View.GONE);
        }else{
            holder.tvLastMessage.setVisibility(View.VISIBLE);
            holder.tvLastMessage.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_face).into(holder.ivProfile);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_face).into(holder.ivProfile);
        }

        //set online status of other user in chatlist
        if(usersList.get(position).getOnlineStatus().equals("online")){
            // online
            holder.ivOnlineStatus.setImageResource(R.drawable.circle_online);

        }else{
            // offline
            holder.ivOnlineStatus.setImageResource(R.drawable.circle_offline);
        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size(); //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder{

        // view of row chatlist.xml
        ImageView ivProfile, ivOnlineStatus;
        TextView tvName, tvLastMessage;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // init view
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivOnlineStatus = itemView.findViewById(R.id.iv_onlineStatus);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_lastMessage);

        }
    }
}
