package com.example.truelove.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;
import com.example.truelove.utilchatuser.ChatMessageView;

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
  /*  public TextView mMessage;
    //    public LinearLayout mContainer;
    public RelativeLayout mContainer;
    CircleImageView circleImageView;
    RelativeLayout relativeLayoutMessage;

    public ChatViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

*//*        mMessage = itemView.findViewById(R.id.message);
        mContainer = itemView.findViewById(R.id.container);*//*
        mMessage = itemView.findViewById(R.id.dialogMessage);
        mContainer = itemView.findViewById(R.id.dialogContainer);
        circleImageView=itemView.findViewById(R.id.dialogAvatar);
        relativeLayoutMessage=itemView.findViewById(R.id.RelativeLayoutdialogMessage);
    }

    @Override
    public void onClick(View view) {
    }*/

    TextView tvMessage, tvTime;
    ImageView ivImage;
    ChatMessageView chatMessageView;

    ChatViewHolders(View itemView) {
        super(itemView);
        chatMessageView = (ChatMessageView) itemView.findViewById(R.id.chatMessageView);
        tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
        tvTime = (TextView) itemView.findViewById(R.id.tv_time);
        ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
    }

    @Override
    public void onClick(View view) {

    }
}
