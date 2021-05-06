package com.example.amr5aled.chatapplication.data;

public class User {

    private String username;
    private String phone;
    private String statues ;
    private String birthday ;

    public User(String username, String phone, String statues, String birthday) {
        this.username = username;
        this.phone = phone;
        this.statues=statues ;
        this.birthday=birthday ;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatues() {
        return statues;
    }

    public void setStatues(String statues) {
        this.statues = statues;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
