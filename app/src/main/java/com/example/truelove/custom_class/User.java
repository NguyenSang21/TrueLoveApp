package com.example.truelove.custom_class;

public class User {

    private String uid;
    private String name;
    private int age;
    private String address;
    private String phone;
    private String email;
    private String img;
    private String sex;

    public  User(){
        super();
    }

    public User(String uid, String name, int age, String address) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public User(String uid, String name, int age, String address, String img, String sex) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.address = address;
        this.sex = sex;
        this.img = img;
    }

    public User(String uid, String name, int age, String address, String img) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.address = address;
        this.img = img;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public User(String uid, String name, int age, String address, String phone, String email, String img) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.img = img;
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

    public String getEmail() {
        return email;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public String getImg() {
        return img;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", img='" + img + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}
