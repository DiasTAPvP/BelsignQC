package com.belman.belsignqc.BE;


import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Photos {
    int id;
    OrderNumbers orderNumber;
    String filepath;
    int uploadedBy;
    Timestamp uploadTime;

    public Photos(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OrderNumbers getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(OrderNumbers orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString()
    {
        String orderNumberStr = orderNumber != null ? orderNumber.getOrderNumber() : "null";
        return "Photo [id=" + id + ", orderNumber=" + orderNumberStr + ", filepath=" + filepath + ", uploadedBy=" + uploadedBy + ", uploadTime=" + uploadTime + "]";
    }
}
