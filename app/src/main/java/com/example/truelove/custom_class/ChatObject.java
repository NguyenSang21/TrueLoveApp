package com.example.truelove.custom_class;

import android.graphics.Bitmap;
import android.net.Uri;

public class ChatObject {
    private String message;
    private Boolean currentUser;
    private Bitmap imgUser;

    public ChatObject(String message, Boolean currentUser, Bitmap urlimage) {
        this.message = message;
        this.currentUser = currentUser;
        this.imgUser = urlimage;
    }

    public ChatObject(String message, Boolean currentUser) {
        this.message = message;
        this.currentUser = currentUser;
    }

    public Bitmap getUrlimage() {
        return imgUser;
    }

    public String getMessage() {
        return message;
    }

    public void setUrlimage(Bitmap urlimage) {
        this.imgUser = urlimage;
    }

    public Boolean getCurrentUser() {
        return currentUser;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCurrentUser(Boolean currentUser) {
        this.currentUser = currentUser;
    }
}
