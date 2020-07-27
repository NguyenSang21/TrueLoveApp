package com.example.truelove.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;
import com.example.truelove.activities.ChatActivity;
import com.example.truelove.custom_class.ChatObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolders>  {
    private List<ChatObject> chatList;
    private Context context;
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    public ChatAdapter(List<ChatObject> dataSetChat, ChatActivity chatActivity) {
        this.chatList = dataSetChat;
        this.context = chatActivity;
    }

    @Override
    public int getItemViewType(int position) {
        ChatObject item = chatList.get(position);

        if (item.getCurrentUser()) return MY_MESSAGE;
        else return OTHER_MESSAGE;
    }

    @NonNull
    @Override
    public ChatViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
/*        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_dialog, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChatViewHolders rcv = new ChatViewHolders(layoutView);
        return rcv;*/

        ChatViewHolders rcv;
        if(viewType == MY_MESSAGE) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_message, parent, false);
       /*     RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);*/
             rcv = new ChatViewHolders(layoutView);
        } else {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_message, parent, false);/*
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);*/
             rcv = new ChatViewHolders(layoutView);
        }
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolders holder, int position) {
       /* holder.mMessage.setText(chatList.get(position).getMessage());
        if(chatList.get(position).getCurrentUser()) {
            holder.relativeLayoutMessage.setGravity(Gravity.END);
            holder.mMessage.setTextColor(Color.parseColor("#ffffff"));
            holder.mMessage.setBackgroundColor(Color.parseColor("#38be55"));

        } else {
            holder.relativeLayoutMessage.setGravity(Gravity.START);
            holder.mMessage.setTextColor(Color.parseColor("#000000"));
            holder.mMessage.setBackgroundColor(Color.parseColor("#ffe680"));
            holder.circleImageView.setImageDrawable(Drawable.createFromPath("./drawable/profile.png"));
            holder.circleImageView.setBackgroundColor(Color.parseColor("#38be55"));
        }*/
//        if (chatMessage.isImage()) {
            holder.ivImage.setVisibility(View.GONE);
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(chatList.get(position).getMessage());
//            holder.ivImage.setImageResource(R.drawable.img_sample);
//        } else {
//            holder.ivImage.setVisibility(View.GONE);
//            holder.tvMessage.setVisibility(View.VISIBLE);

//            holder.tvMessage.setText(chatMessage.getContent());
//        }

        String date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
        holder.tvTime.setText(date);

        holder.chatMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return this.chatList.size();
    }


    public void add(ChatObject message) {
        chatList.add(message);
        notifyItemInserted(chatList.size() - 1);
    }

}
