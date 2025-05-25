package com.belman.belsignqc.DAL.DAO;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.DAL.DBConnector;
import com.belman.belsignqc.DAL.IOrderDataAccess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements IOrderDataAccess {
    private final DBConnector dbConnector;

    public OrderDAO() throws IOException {
        dbConnector = new DBConnector();
    }
   /* @Override
    public List<OrderNumbers> getAllOrders() throws Exception {
        List<OrderNumbers> orders = new ArrayList<>();
        String sql = "SELECT * FROM OrderNumbers";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int ordernumberid = rs.getInt("OrderNumberID");
                String ordernumber = rs.getString("OrderNumber");
                int userid = rs.getInt("UserID");
                Timestamp createdAt = rs.getTimestamp("CreatedAt");

                OrderNumbers order = new OrderNumbers(ordernumberid, ordernumber, userid, createdAt);
                orders.add(order);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Error retrieving orders: " + e.getMessage());
        }
        return orders;
    }*/

    @Override
    public ObservableList<String> getAllOrderNumbers() throws SQLException {
        ObservableList<String> orderNumbers = FXCollections.observableArrayList();
        String sql = "SELECT * FROM OrderNumbers";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
               int ordernumberid = rs.getInt("OrderNumberID");
                String ordernumber = rs.getString("OrderNumber");
                int userid = rs.getInt("UserID");
                Timestamp createdAt = rs.getTimestamp("CreatedAt");

                OrderNumbers order = new OrderNumbers(ordernumberid, ordernumber, userid, createdAt);
                orderNumbers.add(order.getOrderNumber());
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error retrieving order numbers: " + e.getMessage());
        }
        return orderNumbers;
    }
}
