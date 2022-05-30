package com.example.shorts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddPictureRes {

    @SerializedName("info")
    @Expose
    private String info = null;

    @SerializedName("status")
    @Expose
    private Integer status;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public AddPictureRes(String info, int status) {
        this.info=info;
        this.status=status;
    }

}
