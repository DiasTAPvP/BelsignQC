package com.belman.belsignqc.DAL;

import com.belman.belsignqc.BE.OrderNumbers;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public interface IOrderDataAccess {

    /*List<OrderNumbers> getAllOrders() throws Exception;*/

    ObservableList<String> getAllOrderNumbers() throws SQLException;
//
}
