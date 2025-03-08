package com.hackjklu.hackjklu.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("usercred")
public class UserCred {
    public String username;
    public String email;
    public String DOB;
    public String password;
    public String phoneNumber;

    public UserCred(String username, String email, String DOB, String password, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.DOB = DOB;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
