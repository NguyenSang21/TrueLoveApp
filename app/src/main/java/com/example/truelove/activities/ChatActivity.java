package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.adapter.ChatAdapter;
import com.example.truelove.custom_class.ChatObject;
import com.example.truelove.custom_class.FinderDistance;
import com.example.truelove.custom_class.MatchesObject;
import com.example.truelove.custom_class.User;
import com.example.truelove.utilchatuser.KerboardListenerInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements KerboardListenerInterface {
    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList<ChatObject> resultChat = new ArrayList<>();
    private NestedScrollView nestedScrollView;

    private EditText edtSend;
    private ImageButton btnSend;
    private String currentUserID, matchID, chatId;
    private DatabaseReference mDatabaseReference, mDatabaseReferenceChat,databaseReferenceUserCurrent,databaseReferenceUserOther;
    private User userCurrent, userOther;
    private boolean isKeyboardOpen;


    @SuppressLint("RestrictedApi")
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

        nestedScrollView=  findViewById(R.id.scrollViewParent);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager (ChatActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);;

        mAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        recyclerView.setAdapter(mAdapter);

        // handle event on click
        handleOnClick();

        //listener keyboard appear
        setKeyboardVisibilityListener(this);

        personalUI();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            nestedScrollView.post(new Runnable() {
                @Override
                public void run() {
                    nestedScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
            recyclerView.scrollToPosition(mAdapter.getItemCount()+1);
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    recyclerView.scrollToPosition(mAdapter.getItemCount()+1);
                }
            });
            DatabaseReference newMessageDb = mDatabaseReferenceChat.push();
            Map newMessageMap = new HashMap();
            newMessageMap.put("createdByUser", currentUserID);
            newMessageMap.put("text", sendMessageText);
            newMessageDb.setValue(newMessageMap);
        }
        edtSend.setText(null);
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
            if(!"default".equals(userOther.getImg())) {
                bitmapUserOther = BitmapFactory.decodeStream((InputStream)new URL(userOther.getImg()).getContent());
            }
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
                        recyclerView.scrollToPosition(mAdapter.getItemCount()+1);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(mAdapter.getItemCount()+1);
                            }
                        });
                        nestedScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                nestedScrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                        if(isKeyboardOpen){
                            edtSend.post(new Runnable() {
                                @Override
                                public void run() {
                                    edtSend.requestFocus();
                                }
                            });
                        }
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

    private void setKeyboardVisibilityListener(final KerboardListenerInterface onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                onKeyboardVisibilityListener.onVisibilityChanged(isShown);
            }
        });
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
//        Toast.makeText(ChatActivity.this, visible ? "Keyboard is active" : "Keyboard is Inactive", Toast.LENGTH_SHORT).show();
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        /*recyclerView.scrollToPosition(mAdapter.getItemCount());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });*/
//        android:focusableInTouchMode="true"
        if(visible){
            edtSend.post(new Runnable() {
                @Override
                public void run() {
                    edtSend.requestFocus();
                }
            });
        }
        isKeyboardOpen=visible;
    }

    private void personalUI() {

        getSupportActionBar().show();
//        getSupportActionBar().setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            // w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            // w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //w.setStatusBarColor(Color.parseColor("#FB6667"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_math_profire, menu);
        MenuItem menuItem = menu.findItem(R.id.action_profile_match);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_profile_match:
                Toast.makeText(ChatActivity.this, "match ID"+matchID, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this.getApplicationContext(), ProfileFindersActivity.class);
                Bundle b = new Bundle();
                b.putString("matchId", matchID);
                b.putString("chatScreen", "chatScreen");
                intent.putExtras(b);
                startActivityForResult(intent,1111);
                return true;
            case android.R.id.home:
                finish();
                break;

            default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Activity.RESULT_OK==resultCode){
            try{
                String matchNope=data.getExtras().getString("matchNope");
                if(!StringUtils.isEmpty(matchNope)){
                    finish();
                }
            }catch(Exception e){
                System.out.print(e);
            }
        }
    }

    // And override this method
    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}