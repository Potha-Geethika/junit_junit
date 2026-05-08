package com.carbo.admin.model;

public class ChangePassword {

    private String curPassword;

    private String newPassword;

    private String isStrength;



    public String getCurPassword() {
        return curPassword;
    }

    public void setCurPassword(String curPassword) {
        this.curPassword = curPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getIsStrength() {
        return isStrength;
    }

    public void setIsStrength(String isStrength) {
        this.isStrength = isStrength;
    }
}

