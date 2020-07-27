package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.adapter.ChatAdapter;
import com.example.truelove.adapter.MatchesAdapter;
import com.example.truelove.custom_class.ChatObject;
import com.example.truelove.custom_class.MatchesObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView mChatAdapter;
    private ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatObject> resultChat = new ArrayList<>();

    private EditText edtSend;
    private ImageButton btnSend;
    private String currentUserID, matchID, chatId;

    private DatabaseReference mDatabaseReference, mDatabaseReferenceChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mapping();

        matchID = getIntent().getExtras().getString("matchId");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).child("connections").child("matches").child(matchID).child("chatId");
        mDatabaseReferenceChat = FirebaseDatabase.getInstance().getReference().child("chat");

        getChatId();

/*        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(layoutManager);;

        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        recyclerView.setAdapter(mChatAdapter);*/


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        recyclerView.setAdapter(mAdapter);




        // handle event on click
        handleOnClick();
    }

    private void handleOnClick() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void mapping() {
        edtSend = findViewById(R.id.edtSend);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setFocusable(true);
    }

    private void sendMessage() {
        String sendMessageText = edtSend.getText().toString();

        if(!sendMessageText.isEmpty()) {
            DatabaseReference newMessageDb = mDatabaseReferenceChat.push();

            Map newMessageMap = new HashMap();
            newMessageMap.put("createdByUser", currentUserID);
            newMessageMap.put("text", sendMessageText);

            newMessageDb.setValue(newMessageMap);
        }
        edtSend.setText(null);
    }

    private void getChatId() {
      mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists()) {
                chatId = dataSnapshot.getValue().toString();
                  Toast.makeText(ChatActivity.this, ""+ chatId, Toast.LENGTH_SHORT).show();
                mDatabaseReferenceChat = mDatabaseReferenceChat.child(chatId);
                getChatMessage();
              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });
    }

    private void getChatMessage() {
        mDatabaseReferenceChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    String message = null;
                    String createdByUser = null;

                    if(dataSnapshot.child("text").getValue() != null) {
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("createdByUser").getValue().toString() != null) {
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }

                    if(message != null && createdByUser != null) {
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserID)) {
                            currentUserBoolean = true;
                        }

                        ChatObject newMessage = new ChatObject(message, currentUserBoolean);


                        mAdapter.add(newMessage);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<ChatObject> getDataSetChat() {
        return resultChat;
    }
}