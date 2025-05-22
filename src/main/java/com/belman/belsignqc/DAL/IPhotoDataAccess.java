package com.belman.belsignqc.DAL;

import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Product;
import com.belman.belsignqc.BE.Users;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IPhotoDataAccess {

    void insertImagePathToDatabase(Connection connection, List<Path> filePath, Users uploader, Product product) throws SQLException;

    void deleteImageFromDatabase(Photos photos) throws SQLException;

    boolean saveImageAndPath(List<BufferedImage> photos, List<String> fileNames, Users uploader, String productNumber) throws Exception;

    ObservableList<Photos> getImagesForProduct(String productNumber) throws SQLException;

    ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException;

    Product getProductFromNumber(String photoNumber) throws SQLException;
}
