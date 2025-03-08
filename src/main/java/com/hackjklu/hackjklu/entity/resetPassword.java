package com.hackjklu.hackjklu.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("emailotp")
public class resetPassword {
    public String emailid;
    public int otp;

    public resetPassword(String emailid, int otp) {
        this.emailid = emailid;
        this.otp = otp;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }
}
