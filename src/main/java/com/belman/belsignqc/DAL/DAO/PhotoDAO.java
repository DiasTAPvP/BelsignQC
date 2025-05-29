package com.belman.belsignqc.DAL.DAO;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DBConnector;
import com.belman.belsignqc.DAL.IPhotoDataAccess;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PhotoDAO implements IPhotoDataAccess {

    private static DateTimeFormatter Filenameformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

    private DBConnector dbConnector;
    private final String baseRelativePath = "QC_Images";

    public PhotoDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    /**
     * Saves the images and their paths to the database.
     *
     * @param photos      List of BufferedImage objects to be saved.
     * @param fileNames   List of file names for the images.
     * @param uploader    User who uploaded the images.
     * @param orderNumber OrderNumbers object associated with the images.
     * @return true if successful, false otherwise.
     * @throws Exception if an error occurs during the process.
     */

    @Override
    public boolean saveImageAndPath(List<BufferedImage> photos,
                                    List<String> fileNames,
                                    Users uploader,
                                    OrderNumbers orderNumber) throws Exception {

        //check if the lists are of the same size, if not throw an exception.
        if (photos.size() != fileNames.size()) {
            throw new IllegalArgumentException("Photos and paths must be of same size");
        }
        Connection connection = null;
        List<Path> persistedPaths = new ArrayList<>();
//
        try {
            connection = dbConnector.getConnection();
            connection.setAutoCommit(false);

            Path orderFolderPath = Paths.get(baseRelativePath).resolve(orderNumber.getOrderNumber() + "_Images");
            persistedPaths = saveImages(photos, fileNames, orderFolderPath);

            insertImagePathToDatabase(connection, persistedPaths, uploader, orderNumber);

            connection.commit();
            return true;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    //roll back the transaction if ANYTHING goes wrong.
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    //TODO exception handling
                }
            }
            deleteFiles(persistedPaths);
            throw e;
            //TODO exception handling
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    //TODO exception handling
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Saves the images to the specified folder and returns the paths of the saved images.
     *
     * @param photos List of BufferedImage objects to be saved.
     * @param fileNames List of file names for the images.
     * @param orderFolderPath Path to the folder where images will be saved.
     * @return List of paths of the saved images.
     * @throws IOException if an error occurs during the saving process.
     */

    private List<Path> saveImages(List<BufferedImage> photos,
                                  List<String> fileNames,
                                  Path orderFolderPath) throws IOException {
        //if the dir doesn't exist, then make it. if it does, do nothing(IDEMPOTENT).
        Files.createDirectories(orderFolderPath);

        Path tempDir = Files.createTempDirectory(orderFolderPath, "temp_images_");
        List<Path> tempFilePaths = new ArrayList<>(photos.size());
        List<Path> movedFilePaths = new ArrayList<>(photos.size());

        try {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < photos.size(); i++) {
                //give the files unique names using DateTime + index + file extension.
                String uniqueFileName = fileNames.get(i) + "_" + Filenameformatter.format(now) + "_" + i + ".png" ;
                Path tempFilePath = tempDir.resolve(uniqueFileName);
                ImageIO.write(photos.get(i), "png", tempFilePath.toFile());
                tempFilePaths.add(tempFilePath);
            }

            for (Path temp : tempFilePaths) {
                Path dest = orderFolderPath.resolve(temp.getFileName());
                if (Files.exists(dest)) {
                    throw new IOException("File already exists" + dest.toString());
                }
                Files.move(temp, dest, StandardCopyOption.ATOMIC_MOVE);
                movedFilePaths.add(dest);
            }
            Files.deleteIfExists(tempDir);
            return movedFilePaths;
        } catch (IOException e) {
            deleteFiles(movedFilePaths);
            deleteRecursively(tempDir);
            throw e;
        }
    }

    /**
     * Deletes the specified directory and all its contents.
     *
     * @param tempDir Path to the directory to be deleted.
     */

    private void deleteRecursively(Path tempDir) {
        if (tempDir == null || Files.notExists(tempDir)) {
            return;
        }
        //auto close stream after use.
        try (Stream<Path> walk = Files.walk(tempDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignoredException) {
                    //Deletion failure is what it is
                    //ideally its logged or flagged so it can be manually deleted later.
                }
            });
        } catch (IOException ignoredException) {
            //same as previous catch
        }
    }

    /**
     * Deletes the specified files.
     *
     * @param movedFilePaths List of paths of the files to be deleted.
     */

    private void deleteFiles(List<Path> movedFilePaths) {
        for (Path path : movedFilePaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignoredException) {
                //Deletion failure is what it is
                //ideally its logged or flagged so it can be manually deleted later.
            }
        }
    }

    /**
     * Inserts the image paths into the database.
     *
     * @param connection Database connection.
     * @param filePaths List of file paths to be inserted.
     * @param uploader User who uploaded the images.
     * @param orderNumber OrderNumbers object associated with the images.
     * @throws SQLException if an error occurs during the insertion process.
     */

    @Override
    public void insertImagePathToDatabase(Connection connection,
                                          List<Path> filePaths,
                                          Users uploader,
                                          OrderNumbers orderNumber) throws SQLException {

        String sql = "INSERT INTO Pictures (filepath, uploadtime, userid, ordernumberid) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Path path : filePaths) {
                statement.setString(1, path.toString());
                statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                statement.setInt(3, uploader.getUserID());
                statement.setInt(4, orderNumber.getOrderNumberID());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Retrieves all images associated with a specific order number from the database.
     *
     * @param orderNumber The OrderNumbers object containing the order number ID.
     * @return An ObservableList of Photos objects representing the images for the specified order number.
     * @throws SQLException if an error occurs during the database operation.
     */
    @Override
    public ObservableList<Photos> getImagesForOrderNumber(OrderNumbers orderNumber) throws SQLException {
        ObservableList<Photos> photos = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT p.* FROM Pictures p WHERE p.ordernumberid = ?";

        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, orderNumber.getOrderNumberID());

            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                Photos tempImg = new Photos();
                tempImg.setId(rs.getInt("PictureID"));

                // Create OrderNumbers object for this photo
                OrderNumbers photoOrderNumber = new OrderNumbers();
                photoOrderNumber.setOrderNumberID(rs.getInt("OrderNumberID"));
                photoOrderNumber.setOrderNumber(orderNumber.getOrderNumber());
                tempImg.setOrderNumber(photoOrderNumber);

                tempImg.setFilepath(rs.getString("FilePath"));
                tempImg.setUserID(rs.getInt("UserID"));
                tempImg.setUploadTime(new java.sql.Timestamp(rs.getTimestamp("UploadTime").getTime()));
                photos.add(tempImg);
            }
            System.out.println("length:" + photos.size());
            return photos;
        }
    }

    @Override
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException {
        OrderNumbers orderNum = getOrderNumberFromString(orderNumber);
        return getImagesForOrderNumber(orderNum);
    }


    /**
     * Retrieves an OrderNumbers object from the database based on the provided order number string.
     * If the order number does not exist, it creates a new entry in the database.
     *
     * @param orderNumberStr The order number string to search for.
     * @return An OrderNumbers object containing the order number details.
     * @throws SQLException if an error occurs during the database operation.
     */
    @Override
    public OrderNumbers getOrderNumberFromString(String orderNumberStr) throws SQLException {
        OrderNumbers orderNumber = new OrderNumbers();
        String sql = "SELECT * FROM OrderNumbers WHERE ordernumber = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumberStr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                orderNumber.setOrderNumberID(rs.getInt("OrderNumberID"));
                orderNumber.setOrderNumber(rs.getString("OrderNumber"));
                orderNumber.setCreatedAt(rs.getTimestamp("CreatedAt"));
            } else {
                // If order number doesn't exist, create it
                sql = "INSERT INTO OrderNumbers (ordernumber, userid, createdat) VALUES (?, ?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertPs.setString(1, orderNumberStr);
                    insertPs.setInt(2, 1); // Default user ID, should be replaced with actual user
                    insertPs.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                    insertPs.executeUpdate();

                    ResultSet generatedKeys = insertPs.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        orderNumber.setOrderNumberID(generatedKeys.getInt(1));
                    }
                    orderNumber.setOrderNumber(orderNumberStr);
                    orderNumber.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                }
            }
            return orderNumber;
        }
    }

    /**
     * Deletes an image from the database and the file system.
     *
     * @param photo Photos object representing the image to be deleted.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException if an error occurs during the deletion process.
     */
    @Override
    public boolean deleteImageFromDatabase(Photos photo) throws SQLException {
        // First get the file path before we delete the database record
        String filePath = photo.getFilepath();
        boolean success = false;

        String sql = "DELETE FROM Pictures WHERE PictureID = ?";  // Using PictureID based on your schema
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, photo.getId());
            int rowsAffected = ps.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("Photo with id " + photo.getId() + " deleted from database.");

                // Delete the file if database deletion was successful
                try {
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    System.err.println("Could not delete file: " + filePath);
                }
            }
        }

        return success;
    }
}
