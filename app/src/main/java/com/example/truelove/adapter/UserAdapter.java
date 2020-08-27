package com.example.truelove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.custom_class.FinderDistance;
import com.example.truelove.custom_class.User;

import org.w3c.dom.Text;

import java.util.List;

public class UserAdapter extends ArrayAdapter {
    Context context;

    public UserAdapter(@NonNull Context context, int resource, List<FinderDistance> items) {
        super(context, resource, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FinderDistance user = (FinderDistance) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.edtUserName);
        TextView age = convertView.findViewById(R.id.txtAge);
        TextView address = convertView.findViewById(R.id.txtAdrress);
        ImageView image = (ImageView) convertView.findViewById(R.id.img);
        TextView caption = convertView.findViewById(R.id.txtCaption);

        image.setEnabled(false);
        name.setText(user.getUser().getName());
        age.setText(user.getUser().getAge() + " tuổi");
        address.setText(user.getUser().getAddress());
        if(user.getUser().getCaption() == null || user.getUser().getCaption().equals("")) {
            caption.setText("Thích chó , mèo");
        } else {
            caption.setText(user.getUser().getCaption());
        }

        Glide.with(getContext()).load(user.getUser().getImg()).into(image);

        return convertView;
    }
}
