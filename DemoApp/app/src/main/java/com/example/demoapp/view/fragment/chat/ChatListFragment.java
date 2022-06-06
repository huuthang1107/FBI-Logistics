package com.example.demoapp.view.fragment.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.demoapp.adapter.chat.ChatListAdapter;
import com.example.demoapp.databinding.FragmentChatListBinding;
import com.example.demoapp.model.ChatList;
import com.example.demoapp.model.Chats;
import com.example.demoapp.model.Users;
import com.example.demoapp.view.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    // firebase auth
    FirebaseAuth mAuth;
    FragmentChatListBinding binding;
    List<ChatList> chatListList;
    List<Users> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    ChatListAdapter chatListAdapter;

    public ChatListFragment(){}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatListBinding.inflate(inflater, container, false);

        // init
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        chatListList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatListList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ChatList chatList = ds.getValue(ChatList.class);
                    chatListList.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Users users = ds.getValue(Users.class);
                    for(ChatList chatList: chatListList){
                        if(users.getUid() != null && users.getUid().equals(chatList.getId())){
                            usersList.add(users);
                            break;
                        }
                    }
                    //adapter
                    chatListAdapter = new ChatListAdapter(getContext(), usersList);
                    //setadapter
                    binding.recyclerView.setAdapter(chatListAdapter);
                    //set last message
                    for(int i=0; i<usersList.size(); i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMesage = "default";
                for(DataSnapshot ds: snapshot.getChildren()){
                    Chats chats = ds.getValue(Chats.class);
                    if(chats == null){
                        continue;
                    }
                    String sender = chats.getSender();
                    String receiver = chats.getReceiver();
                    if(sender == null || receiver == null){
                        continue;
                    }
                    if(chats.getReceiver().equals(currentUser.getUid()) &&
                            chats.getSender().equals(userId) ||
                    chats.getReceiver().equals(userId) &&
                    chats.getSender().equals(currentUser.getUid())){
                        theLastMesage = chats.getMessage();
                    }
                }
                chatListAdapter.setLastMessageMap(userId, theLastMesage);
                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}