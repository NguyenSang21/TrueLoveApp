package com.example.truelove.custom_class;

public class Album {
    String imageUrl;
    String idStore;

    public Album(String imageUrl, String idStore) {
        this.imageUrl = imageUrl;
        this.idStore = idStore;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setIdStore(String idStore) {
        this.idStore = idStore;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getIdStore() {
        return idStore;
    }

    public Album() {
    }
}