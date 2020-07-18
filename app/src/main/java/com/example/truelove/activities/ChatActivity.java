package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.truelove.R;
import com.example.truelove.adapter.ChatAdapter;
import com.example.truelove.adapter.MatchesAdapter;
import com.example.truelove.custom_class.ChatObject;
import com.example.truelove.custom_class.MatchesObject;
import com.google.firebase.auth.FirebaseAuth;
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
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatObject> resultChat = new ArrayList<>();

    private EditText edtSend;
    private Button btnSend;
    private String currentUserID, matchID, chatId;

    private DatabaseReference mDatabaseReference, mDatabaseReferenceChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mapping();

        matchID = getIntent().getExtras().getString("matchId");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).child("matches").child(matchID).child("chatId");
        mDatabaseReferenceChat = FirebaseDatabase.getInstance().getReference().child("chat");

        getChatId();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        recyclerView.setAdapter(mChatAdapter);

        // handle event on click
        handleOnClick();
    }

    private void handleOnClick() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void mapping() {
        edtSend = findViewById(R.id.edtSend);
        btnSend = findViewById(R.id.btnSend);
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
                mDatabaseReferenceChat = mDatabaseReferenceChat.child(chatId);
              }
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