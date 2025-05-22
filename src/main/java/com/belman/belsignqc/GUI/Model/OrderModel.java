package com.belman.belsignqc.GUI.Model;

public class OrderModel {
    private String orderNumber;

    public OrderModel(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

}
