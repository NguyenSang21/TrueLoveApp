package com.example.truelove.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.burhanrashid52.photoeditor.EditImageActivity;
import com.example.truelove.R;
import com.example.truelove.activities.ProfileActivity;
import com.example.truelove.activities.ProfileActivity_view;
import com.example.truelove.custom_class.Album;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>   {
    private ArrayList<Album> imageUrls;
    private Context context;
    private ProfileActivity actitityProfire;
    private Activity actitityCurrent;
    private String userIdMatch;

    public AlbumAdapter(Context context, ArrayList<Album> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public void setActitityProfire(ProfileActivity actitityS){
        this.actitityProfire=actitityS;
    }
    public void setActitityCurrent(Activity actitityS){
        this.actitityCurrent=actitityS;
    }

    public void setUserIdMatch(String id){
        this.userIdMatch=id;
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album_item, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * gets the image url from adapter and passes to Glide API to load the image
     *
     * @param viewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        Glide.with(context).load(imageUrls.get(i).getImageUrl()).into(viewHolder.img);
        if(actitityProfire!=null){ // view profire
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actitityProfire.modeImage=2;
                    Toast.makeText(context , "viewHolder "+imageUrls.get(i).getIdStore(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, EditImageActivity.class);
                    intent.putExtra("imageUri", imageUrls.get(i).getImageUrl());
                    intent.putExtra("storeIDExist", imageUrls.get(i).getIdStore());
                    actitityProfire.startActivityForResult(intent, 1);
                }
            });
        }else{
            // view finder
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProfileActivity_view.class);
                    intent.putExtra("imageUri", imageUrls.get(i).getImageUrl());
                    intent.putExtra("storeIDExist", imageUrls.get(i).getIdStore());
                    intent.putExtra("userIdMatch", userIdMatch);
                    actitityCurrent.startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.imageView);
        }
    }


}
