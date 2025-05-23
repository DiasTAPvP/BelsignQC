package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Exceptions.CameraNotFound;
import com.belman.belsignqc.BLL.OpenCV;
import com.belman.belsignqc.BLL.PhotoTaking;
import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.GUI.Model.PhotoModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraController extends BaseController implements Initializable {

    @FXML
    public ImageView imgCamera;
    @FXML
    public Button btnPhotoLogout;
    @FXML
    public ImageView imgPreview2;
    @FXML
    public ImageView imgPreview1;
    @FXML
    public Button btnConfirmation;
    @FXML
    public StackPane cameraStackpane;
    @FXML
    public StackPane rootPane;
    @FXML
    public ImageView imgFullPreview;
    @FXML
    public HBox previewControls;
    @FXML
    public Button btnDeletePreview;
    @FXML
    public Button btnClosePreview;
    @FXML
    public Button btnTakePicture;

    private ScheduledExecutorService mainPreviewExecutor;
    private PhotoTaking strategy;

    private final ArrayDeque<Image> gallery = new ArrayDeque<>();
    private PhotoModel photoModel;
    private List<Image> imagesToSave = new ArrayList<>();
    private String productNumber;
    private int currentPreviewIndex = -1;

    public CameraController() {
        photoModel = new PhotoModel();
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cameraStackpane.widthProperty());
        clip.heightProperty().bind(cameraStackpane.heightProperty());
        cameraStackpane.setClip(clip);

        imgCamera.setManaged(false);

        bindPreviewToRoot();
        bindCameraViewToRoot();

        imgPreview1.setOnMouseClicked(e -> openOverlayPreview(0));
        imgPreview2.setOnMouseClicked(e -> openOverlayPreview(1));

        strategy = new OpenCV();
        try {
            strategy.start();
        } catch (CameraNotFound e) {
            System.err.println("Camera not found: " + e.getMessage());
            //TODO alert
        } catch (Exception e) {
            System.err.println("Error starting camera: " + e.getMessage());
            //TODO alert blank frames VISUALLY INFORM THE USER THAT NO CAMERA WAS FOUND USING AN IMAGE ON THE IMAGEVIEW
            // SAYING - NO CAMERA FOUND
        }
        mainPreviewExecutor = Executors.newSingleThreadScheduledExecutor();
        mainPreviewExecutor.scheduleAtFixedRate(() -> {
            try {
                Image frame = strategy.takePhoto();
                Platform.runLater(() -> {
                    imgCamera.setImage(frame);
                    adjustImage(imgCamera, cameraStackpane);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //30 fps
        }, 0, 33, TimeUnit.MILLISECONDS);

        btnConfirmation.setDisable(true);
    }


    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
    }

    private void bindCameraViewToRoot() {
        cameraStackpane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackpane);
        });
        cameraStackpane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackpane);
        });
    }

    private void adjustImage(ImageView imageView, StackPane container) {
        Image frame = imageView.getImage();
        double paneHeight = container.getHeight();
        double paneWidth = container.getWidth();
        if (frame == null || paneHeight <= 0 || paneWidth <= 0) {
            return;
        }

        double scale = Math.max(paneWidth / frame.getWidth(), paneHeight / frame.getHeight());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(frame.getWidth() * scale);
        imageView.setFitHeight(frame.getHeight() * scale);
    }

    private void captureImage() {
        try {
            Image image = strategy.takePhoto();
            sendToGallery(image);
            imagesToSave.add(image);
            btnConfirmation.setDisable(false);


            //TODO 1. store image in a list, could be done through the sendToGallery method.

            //TODO 2. make a save button that calls photoDAO (not directly) to save the images.

        } catch (Exception e) {
            System.err.println("Error capturing image: " + e.getMessage());
            //TODO alert
        }
    }

    private void sendToGallery(Image image) {
        if (gallery.size() == 2) {
            gallery.removeLast();
        }
        gallery.addFirst(image);

        imgPreview1.setImage((gallery.size() > 0 ? gallery.toArray(new Image[0])[0] : null));
        imgPreview2.setImage((gallery.size() > 1 ? gallery.toArray(new Image[0])[1] : null));
    }


    @FXML
    public void handleFinishCamera(ActionEvent actionEvent) {
        if (imagesToSave.isEmpty()) {
            return;
        }

        // TODO: Implement image saving functionality when PhotoModel is ready
        // For now, just log that images would be saved
        System.out.println("Would save " + imagesToSave.size() + " images for product number: " + productNumber);

        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                System.err.println("Error stopping camera: " + e.getMessage());
                //TODO exception
            }
        }

        // Navigate back to the previous screen
        screenManager.setScreen("photo_doc");
    }

    @FXML
    public void handleLogOut(ActionEvent actionEvent) {
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                System.err.println("Error stopping camera: " + e.getMessage());
                //TODO exception
            }
        }

        // Navigate back to the previous screen
        screenManager.setScreen("photo_doc");
    }

    private void openOverlayPreview(int i) {
        Image[] images = gallery.toArray(new Image[0]);
        if (i < images.length) {
            imgFullPreview.setImage(images[i]);
            imgFullPreview.setVisible(true);
            previewControls.setVisible(true);
            btnConfirmation.setVisible(false);
            btnPhotoLogout.setVisible(false);
            btnTakePicture.setVisible(false);
            currentPreviewIndex = i;

            adjustImage(imgFullPreview, rootPane);
        }
    }
    @FXML
    public void handleDeletePreview(ActionEvent actionEvent) {
       deletePreview();
    }

    private void updatePreviews() {
        Image[] images = gallery.toArray(new Image[0]);
        imgPreview1.setImage((images.length > 0 ? images[0] : null));
        imgPreview2.setImage((images.length > 1 ? images[1] : null));
    }

    public void deletePreview() {
        if (currentPreviewIndex < 0) {
            return;
        }
        Image[] images = gallery.toArray(new Image[0]);
        Image imageToDelete = images[currentPreviewIndex];

        gallery.remove(imageToDelete);
        if (currentPreviewIndex < imagesToSave.size()) {
            imagesToSave.remove(currentPreviewIndex);
        }
        updatePreviews();
        closePreview();
    }

    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {
        closePreview();
    }

    public void closePreview() {
        imgFullPreview.setVisible(false);
        previewControls.setVisible(false);
        btnConfirmation.setVisible(true);
        btnPhotoLogout.setVisible(true);
        btnTakePicture.setVisible(true);
        currentPreviewIndex = -1;
    }

    @FXML
    public void handleCaptureImage(ActionEvent actionEvent) {
        captureImage();
    }
}
