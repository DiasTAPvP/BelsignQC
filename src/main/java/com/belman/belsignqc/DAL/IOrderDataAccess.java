package com.belman.belsignqc.DAL;

import com.belman.belsignqc.BE.OrderNumbers;

import java.util.List;

public interface IOrderDataAccess {

    List<OrderNumbers> getAllOrders() throws Exception;
//
}
