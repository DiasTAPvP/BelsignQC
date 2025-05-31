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

    /**
     * Saves a list of images and their corresponding file names to the database.
     *
     * @param images        List of BufferedImage objects to be saved.
     * @param fileNames     List of file names corresponding to the images.
     * @param uploader      User who is uploading the images.
     * @param orderNumberStr Order number as a string.
     * @return true if the images and paths were saved successfully, false otherwise.
     * @throws Exception if an error occurs during the save operation.
     */
    public boolean saveImageAndPath(List<BufferedImage> images,
                                    List<String> fileNames,
                                    Users uploader,
                                    String orderNumberStr) throws Exception {
        OrderNumbers orderNumber = photoDataAccess.getOrderNumberFromString(orderNumberStr);
        return photoDataAccess.saveImageAndPath(images, fileNames, uploader, orderNumber);
    }

    /**
     * Retrieves all images from the database.
     *
     * @return ObservableList of Photos containing all images.
     * @throws SQLException if an error occurs while retrieving images.
     */
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException {
        return photoDataAccess.getImagesForOrder(orderNumber);
    }

    /**
     * Deletes an image from the database.
     *
     * @param photo The Photos object representing the image to be deleted.
     * @return true if the image was deleted successfully, false otherwise.
     * @throws SQLException if an error occurs during the deletion.
     */
    public boolean deleteImage(Photos photo) throws SQLException {
        return photoDataAccess.deleteImageFromDatabase(photo);
    }

    /**
     * Retrieves images for a specific order number.
     *
     * @param orderNumberStr The order number as a string.
     * @return ObservableList of Photos associated with the specified order number.
     * @throws SQLException if an error occurs while retrieving images.
     */
    public ObservableList<Photos> getImagesForOrderNumber(String orderNumberStr) throws SQLException {
        OrderNumbers orderNumber = photoDataAccess.getOrderNumberFromString(orderNumberStr);
        return photoDataAccess.getImagesForOrderNumber(orderNumber);
    }
}
