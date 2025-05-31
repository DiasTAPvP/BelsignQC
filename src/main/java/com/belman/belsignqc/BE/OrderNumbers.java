package com.belman.belsignqc.BE;


import java.sql.Timestamp;

public class OrderNumbers {
    private int orderNumberID;
    private String orderNumber;
    private int userID;
    private Timestamp createdAt;

    // Default constructor
    public OrderNumbers() {
    }

    // Constructor
    public OrderNumbers(int orderNumberID, String orderNumber, int userID, Timestamp createdAt) {
        this.orderNumberID = orderNumberID;
        this.orderNumber = orderNumber;
        this.userID = userID;
        this.createdAt = createdAt;
    }

    // Getter and Setter for orderNumberID
    public int getOrderNumberID() {
        return orderNumberID;
    }

    public void setOrderNumberID(int orderNumberID) {
        this.orderNumberID = orderNumberID;
    }

    // Getter and Setter for orderNumber
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    // Getter and Setter for userID
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    // Getter and Setter for createdAt
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
