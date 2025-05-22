package com.belman.belsignqc.BLL;

import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DAO.PhotoDAO;
import com.belman.belsignqc.DAL.IPhotoDataAccess;
import javafx.collections.ObservableList;


import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

public class PhotoManger {
    private IPhotoDataAccess photoDataAccess;

    public PhotoManager() throws Exception {
        try {
            photoDataAccess = new PhotoDAO();
        } catch (Exception e) {
            throw new Exception();
            //TODO exception handling
        }
    }

    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    Users uploader,
                                    String productNumber) throws Exception {
        return photoDataAccess.saveImageAndPath(images, fileNames, uploader, productNumber);
    }
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException {
        return photoDataAccess.getImagesForOrder(orderNumber);
    }

    public void deleteImage(Photos photos) throws SQLException {
        photoDataAccess.deleteImageFromDatabase(photos);
    }

    public ObservableList<Photos> getImagesForProduct(String productNumber) throws SQLException {
        return photoDataAccess.getImagesForProduct(productNumber);
    }
}
