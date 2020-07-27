package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.adapter.ChatAdapter;
import com.example.truelove.custom_class.ChatObject;
import com.example.truelove.custom_class.MatchesObject;
import com.example.truelove.custom_class.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatObject> resultChat = new ArrayList<>();

    private EditText edtSend;
    private ImageButton btnSend;
    private String currentUserID, matchID, chatId;
    private DatabaseReference mDatabaseReference, mDatabaseReferenceChat,databaseReferenceUserCurrent,databaseReferenceUserOther;
    private User userCurrent, userOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mapping();

        matchID = getIntent().getExtras().getString("matchId");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).child("connections").child("matches").child(matchID).child("chatId");
        mDatabaseReferenceChat = FirebaseDatabase.getInstance().getReference().child("chat");
        databaseReferenceUserCurrent = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
        databaseReferenceUserOther = FirebaseDatabase.getInstance().getReference().child("users").child(matchID);

        getChatId();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(layoutManager);;

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
        if( mAdapter.getItemCount() !=0 && recyclerView != null){

            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);

        }
    }

    private void getChatId() {
        // set information user current
        databaseReferenceUserCurrent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userCurrent = new User();
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        userCurrent.setName(map.get("name").toString());
                    }
                    if (map.get("age") != null) {
                        userCurrent.setAge(Integer.valueOf(map.get("age").toString()));
                    }
                    if (map.get("address") != null) {
                        userCurrent.setAddress(map.get("address").toString());
                    }
                    if (map.get("phone") != null) {
                        userCurrent.setPhone(map.get("phone").toString());
                    }
                    if (map.get("email") != null) {
                        userCurrent.setImg(map.get("email").toString());
                    }
                    if (map.get("img") != null) {
                        String uriImage = map.get("img").toString();
                        userCurrent.setImg(uriImage);
                    }
                    if (map.get("sex") != null) {
                        String uriImage = map.get("sex").toString();
                        if (uriImage == null) {
                            return;
                        } else if (uriImage.equals("male")) {
//                            sex = "male";
                            userCurrent.setSex("Nam");
                        } else if (uriImage.equals("female")) {
//                            sex = "female";
                            userCurrent.setSex("Nữ");
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // get infor of userOther chating with user current
        fetchMatchInformation();


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
        // img for user current
        Bitmap bitmapUserCurrent = null;
        try {
            bitmapUserCurrent = BitmapFactory.decodeStream((InputStream)new URL(userCurrent.getImg()).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // img for user Other
        Bitmap bitmapUserOther = null;
        try {
            bitmapUserOther = BitmapFactory.decodeStream((InputStream)new URL(userOther.getImg()).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Bitmap finalBitmapUserCurrent = bitmapUserCurrent;
        final Bitmap finalBitmapUserOther = bitmapUserOther;
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
                        ChatObject newMessage;
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserID)) {
                            currentUserBoolean = true;
                            newMessage = new ChatObject(message, currentUserBoolean, finalBitmapUserCurrent);
                        }else{
                            currentUserBoolean = false;
                            newMessage = new ChatObject(message, currentUserBoolean, finalBitmapUserOther);
                        }
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

    // get userOther chating with user current
    private void fetchMatchInformation() {
        databaseReferenceUserOther.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";

                    if(dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }

                    if(dataSnapshot.child("img").getValue() != null) {
                        profileImageUrl = dataSnapshot.child("img").getValue().toString();
                    }

                    MatchesObject obj = new MatchesObject(userId, name, profileImageUrl);

                    userOther = new User();
                    userOther.setName(name);
                    userOther.setUid(userId);
                    userOther.setImg(profileImageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}