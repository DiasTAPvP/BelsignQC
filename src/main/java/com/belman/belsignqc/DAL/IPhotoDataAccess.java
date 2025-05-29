package com.belman.belsignqc.DAL;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IPhotoDataAccess {

    void insertImagePathToDatabase(Connection connection, List<Path> filePath, Users uploader, OrderNumbers orderNumber) throws SQLException;

    boolean deleteImageFromDatabase(Photos photo) throws SQLException;

    boolean saveImageAndPath(List<BufferedImage> photos, List<String> fileNames, Users uploader, OrderNumbers orderNumber) throws Exception;

    ObservableList<Photos> getImagesForOrderNumber(OrderNumbers orderNumber) throws SQLException;

    ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException;

    OrderNumbers getOrderNumberFromString(String orderNumberStr) throws SQLException;
}
