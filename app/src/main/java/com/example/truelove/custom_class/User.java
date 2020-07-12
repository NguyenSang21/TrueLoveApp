package com.example.truelove.custom_class;

public class User {

    private String uid;
    private String name;
    private int age;
    private String address;


    public User(String uid, String name, int age, String address) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
