package com.example.truelove.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.burhanrashid52.photoeditor.EditImageActivity;
import com.example.truelove.R;
import com.example.truelove.activities.ProfileActivity;
import com.example.truelove.custom_class.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumAdapter_finders extends RecyclerView.Adapter<AlbumAdapter_finders.ViewHolder>   {
    private List<Album> imageUrls;
    private Context context;
    ImageView imgFull;
    RecyclerView thisrecyclerView;
    public  AlbumAdapter_finders.ViewHolder viewHolderCurrent=null;
    private Map<String, AlbumAdapter_finders.ViewHolder> dsView= new HashMap<String, AlbumAdapter_finders.ViewHolder>();

    public AlbumAdapter_finders(Context context, List<Album> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public void setImgFull(ImageView imgFull) {
        this.imgFull = imgFull;
    }

    public void setRecyclerViews(RecyclerView recyclerView){
        this.thisrecyclerView=recyclerView;
    }

    @Override
    public AlbumAdapter_finders.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album_item_finders, viewGroup, false);
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
        Glide.with(context).load(imageUrls.get(i).getImageUrl()).into(viewHolder.imgMin);
        final int[] row_index = {0};
        viewHolder.borderImageMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(context).load(imageUrls.get(i).getImageUrl()).into(imgFull);
                viewHolder.borderImageMin.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.borderImageMin.setBackgroundColor(Color.parseColor("#F45F92"));
                    }
                });
                AlbumAdapter_finders.ViewHolder viewHolderCurrent=viewHolder;
                resetBoderImageMin(viewHolderCurrent);
            }
        });

       /* if(row_index[0] ==i){
            viewHolder.borderImageMin.setBackgroundColor(Color.parseColor("#F45F92"));
        }
        else
        {
            viewHolder.borderImageMin.setBackgroundColor(Color.parseColor("#ffffff"));
        }*/

        dsView.put(imageUrls.get(i).getIdStore(),viewHolder);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMin;
        LinearLayout borderImageMin;

        public ViewHolder(View view) {
            super(view);
            imgMin = view.findViewById(R.id.imageViewMin);
            borderImageMin=view.findViewById(R.id.borderImageMin);
        }
    }

    public void resetBoderImageMin(AlbumAdapter_finders.ViewHolder viewHolderCurrents) {
       if (dsView != null && dsView.size() != 0 && viewHolderCurrents != null) {
            for (Map.Entry<String, AlbumAdapter_finders.ViewHolder> entry : dsView.entrySet()) {
                if(!viewHolderCurrents.equals(entry.getValue())){
                    entry.getValue().borderImageMin.setBackgroundColor(Color.WHITE);
                }
            }
        }
    }

    public void setInitListImage(int index, String storeuser){
        AlbumAdapter_finders.ViewHolder viewHolder=getViewViewHolderByStoreId(storeuser);
        viewHolder.borderImageMin.setBackgroundColor(Color.WHITE);
        Glide.with(context).load(imageUrls.get(index).getImageUrl()).into(imgFull);
        viewHolder.borderImageMin.setBackgroundColor(Color.parseColor("#F45F92"));
        viewHolderCurrent= viewHolder;
    }

    AlbumAdapter_finders.ViewHolder getViewViewHolderByStoreId(String storeId){
        for (Map.Entry<String, AlbumAdapter_finders.ViewHolder> entry : dsView.entrySet()) {
            if(storeId.equals(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }
}
