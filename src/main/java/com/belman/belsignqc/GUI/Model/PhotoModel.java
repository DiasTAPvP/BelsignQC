package com.belman.belsignqc.GUI.Model;

import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.PhotoManager;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

public class PhotoModel {
    private PhotoManager photoManager;

    public PhotoModel() {
        try {
            photoManager = new PhotoManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    Users uploader,
                                    String orderNumber) throws Exception {
        return photoManager.saveImageAndPath(images, fileNames, uploader, orderNumber);
    }
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws Exception {
        return photoManager.getImagesForOrder(orderNumber);
    }
    public ObservableList<Photos> getImagesForOrderNumber(String orderNumberStr) throws SQLException {
        return photoManager.getImagesForOrderNumber(orderNumberStr);
    }

    public void deleteImage(Photos photos) throws SQLException {
        photoManager.deleteImage(photos);
    }
}
