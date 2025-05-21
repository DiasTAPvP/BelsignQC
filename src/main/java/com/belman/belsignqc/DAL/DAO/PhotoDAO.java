package com.belman.belsignqc.DAL.DAO;

import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Product;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DBConnector;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.collections.ObservableList;
import org.opencv.photo.Photo;

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

public class PhotoDAO {

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
     * @param productNumber Product number associated with the images.
     * @return true if successful, false otherwise.
     * @throws Exception if an error occurs during the process.
     */

    public boolean saveImageAndPath(List<BufferedImage> photos,
                                    List<String> fileNames,
                                    Users uploader,
                                    String productNumber) throws Exception {

        //check if the lists are of the same size, if not throw an exception.
        if (photos.size() != fileNames.size()) {
            throw new IllegalArgumentException("Photos and paths must be of same size");
        }
        Connection connection = null;
        List<Path> persistedPaths = new ArrayList<>();

        try {
            connection = dbConnector.getConnection();
            connection.setAutoCommit(false);

            Path orderFolderPath = baseRelativePath.resolve(productNumber + "_Images");
            persistedPaths = saveImages(photos, fileNames, orderFolderPath);

            insertImagePathToDatabase(connection, persistedPaths, uploader, getProductFromNumber(productNumber));

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
     * @param product Product associated with the images.
     * @throws SQLException if an error occurs during the insertion process.
     */

    @Override
    public void insertImagePathToDatabase(Connection connection,
                                          List<Path> filePaths,
                                          Users uploader,
                                          Product product) throws SQLException {

        String sql = "INSERT INTO Photos (product_id, file_path, uploaded_by, uploaded_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Path path : filePaths) {
                statement.setInt(1, product.getId());
                statement.setString(2, path.toString());
                statement.setInt(3, uploader.getId());
                statement.setObject(4, LocalDateTime.now());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public ObservableList<Photos> getImagesForProduct(String productNumber) throws SQLException {
        ObservableList<Photos> photos = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT p.* FROM Photos p INNER JOIN Products pr ON p.product_id = pr.id WHERE pr.products_order_number = ?";


        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, productNumber);

            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                Photos tempImg = new Photos();
                tempImg.setId(rs.getInt("id"));
                tempImg.setOrderNumber(rs.getString("product_id"));
                tempImg.setFilepath(rs.getString("file_path"));
                tempImg.setUploadedBy(rs.getInt("uploaded_by"));
                tempImg.setUploadTime(rs.getObject("uploaded_at", LocalDateTime.class));
                photos.add(tempImg);
            }
            System.out.println("length:" + photos.size());
            return photos;
        }
    }

    @Override
    public ObservableList<Photos> getImagesForOrder(String orderNumber) throws SQLException {
        //todo: make this??
        return null;
    }

    @Override
    public Product getProductFromNumber(String productNumber) throws SQLException {
        Product product = new Product();
        String sql = "SELECT * FROM Products WHERE products_order_number = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                product.setId(rs.getInt("id"));
                product.setOrder_id(rs.getInt("order_id"));
                product.setProduct_number(rs.getString("products_order_number"));
            }
            return product;
        }

    }

    @Override
    public void deleteImageFromDatabase(Photos photo) throws SQLException {
        String sql = "DELETE FROM Photos WHERE id = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, photo.getId());
            ps.executeUpdate();
            System.out.println("photo with id " + photo.getId() + " deleted from database.");
        }
        catch(SQLServerException e){
            throw new SQLException(e);
        }

        try{
            Files.deleteIfExists(Paths.get(photo.getFilepath()));
        } catch (IOException e) {
            //TODO Burde nok smide et eller andet.. kører bare runtime for nu.
            throw new RuntimeException(e);
        }
    }
}
