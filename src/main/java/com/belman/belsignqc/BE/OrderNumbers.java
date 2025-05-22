package com.belman.belsignqc.BE;

import java.sql.Timestamp;

public class OrderNumbers {

    private int orderNumberID;
    private String orderNumber;
    private Timestamp createdAt;

    //Constructor
    public OrderNumbers(int orderNumberID, String orderNumber, Timestamp createdAt) {
        this.orderNumberID = orderNumberID;
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
    }

    //Getters and setters
    public int getOrderNumberID() {
        return orderNumberID;
    }
    public void setOrderNumberID(int orderNumberID) {
        this.orderNumberID = orderNumberID;
    }
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}


