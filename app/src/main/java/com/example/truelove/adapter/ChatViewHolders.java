package com.example.truelove.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;
import com.example.truelove.activities.ChatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mMessage;
    //    public LinearLayout mContainer;
    public RelativeLayout mContainer;
    CircleImageView circleImageView;
    RelativeLayout relativeLayoutMessage;

    public ChatViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

/*        mMessage = itemView.findViewById(R.id.message);
        mContainer = itemView.findViewById(R.id.container);*/
        mMessage = itemView.findViewById(R.id.dialogMessage);
        mContainer = itemView.findViewById(R.id.dialogContainer);
        circleImageView=itemView.findViewById(R.id.dialogAvatar);
        relativeLayoutMessage=itemView.findViewById(R.id.RelativeLayoutdialogMessage);
    }

    @Override
    public void onClick(View view) {
    }
}
