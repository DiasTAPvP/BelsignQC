package com.belman.belsignqc.BLL;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DAO.PhotoDAO;
import com.belman.belsignqc.DAL.IPhotoDataAccess;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

public class PhotoManager {
    private IPhotoDataAccess photoDataAccess;

    public PhotoManager() throws Exception {
        photoDataAccess = new PhotoDAO(); // PhotoDAO now implements IPhotoDataAccess
    }

    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    Users uploader,
                                    String orderNumberStr) throws Exception {
        OrderNumbers orderNumber = photoDataAccess.getOrderNumberFromString(orderNumberStr);
        return photoDataAccess.saveImageAndPath(images, fileNames, uploader, orderNumber);
    }
//
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException {
        return photoDataAccess.getImagesForOrder(orderNumber);
    }

    public void deleteImage(Photos photos) throws SQLException {
        photoDataAccess.deleteImageFromDatabase(photos);
    }

    public ObservableList<Photos> getImagesForOrderNumber(String orderNumberStr) throws SQLException {
        OrderNumbers orderNumber = photoDataAccess.getOrderNumberFromString(orderNumberStr);
        return photoDataAccess.getImagesForOrderNumber(orderNumber);
    }
}
