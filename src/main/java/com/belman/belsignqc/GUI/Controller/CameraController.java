package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Exceptions.CameraNotFound;
import com.belman.belsignqc.BLL.OpenCV;
import com.belman.belsignqc.BLL.PhotoTaking;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.GUI.Model.PhotoModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
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

    /**
     * Called when the camera screen is activated.
     * Reinitializes the camera to ensure it's working properly when returning to this screen.
     */
    @Override
    public void onScreenActivated() {
        // Reinitialize the camera when returning to this screen
        initializeCamera();
    }

    private ScheduledExecutorService mainPreviewExecutor;
    private PhotoTaking strategy;

    private final ArrayDeque<Image> gallery = new ArrayDeque<>();
    private PhotoModel photoModel;
    private List<BufferedImage> imagesToSave = new ArrayList<>();
    private String orderNumber;
    private int currentPreviewIndex = -1;
    private Users currentUser;

    public CameraController() {
        photoModel = new PhotoModel();
    }

    /**
     * Initializes the camera controller.
     * This method sets up the camera view, binds it to the root pane, and configures event handlers.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object does not need localization.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize currentUser from UserSession
        currentUser = UserSession.getInstance().getUser();
        System.out.println("Current user on init: " + (currentUser != null ? currentUser.getUserID() : "null"));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cameraStackpane.widthProperty());
        clip.heightProperty().bind(cameraStackpane.heightProperty());
        cameraStackpane.setClip(clip);

        imgCamera.setManaged(false);

        bindPreviewToRoot();
        bindCameraViewToRoot();

        imgPreview1.setOnMouseClicked(e -> openOverlayPreview(0));
        imgPreview2.setOnMouseClicked(e -> openOverlayPreview(1));

        // Add click handler to full preview image to cycle through previews
        imgFullPreview.setOnMouseClicked(e -> {
            if (currentPreviewIndex >= 0) {
                int nextIndex = (currentPreviewIndex + 1) % gallery.size();
                openOverlayPreview(nextIndex);
            }
        });

        // Add tooltip to indicate that clicking cycles through images
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Click to view next image");
        javafx.scene.control.Tooltip.install(imgFullPreview, tooltip);

        initializeCamera();

        btnConfirmation.setDisable(true);
    }

    // This method is called when the CameraController receives data from the OperatorController
    @Override
    public void receiveData(Object data) {
        if (data instanceof String) {
            this.orderNumber = (String) data;
            System.out.println("Received order number: " + orderNumber);
        }
    }

    /**
     * Initializes or reinitializes the camera.
     * This method can be called when returning to the camera screen to ensure the camera is properly initialized.
     * Also clears any existing image previews.
     */
    private void initializeCamera() {
        // Shutdown any existing camera resources first
        shutdownCamera();

        // Clear the gallery and image previews
        gallery.clear();
        imagesToSave.clear();
        imgPreview1.setImage(null);
        imgPreview2.setImage(null);
        imgFullPreview.setImage(null);
        btnConfirmation.setDisable(true);

        // Initialize the camera strategy
        strategy = new OpenCV();
        try {
            strategy.start();
        } catch (CameraNotFound e) {
            System.err.println("Camera not found: " + e.getMessage());
            showAlert.display("Camera Not Found", "No camera was detected. Please connect a camera and try again.");
            return;
        } catch (Exception e) {
            System.err.println("Error starting camera: " + e.getMessage());
            showAlert.display("Camera Error", "An error occurred while starting the camera: " + e.getMessage());
            // TODO: Display a placeholder image on the imageview indicating no camera was found
            return;
        }

        // Start the preview executor
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
    }


    /**
     * Binds the preview image to the root pane to ensure it resizes correctly.
     * This method adjusts the image size based on the root pane dimensions.
     */
    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
    }

    /**
     * Binds the camera view to the StackPane to ensure it resizes correctly.
     * This method adjusts the camera image size based on the StackPane dimensions.
     */
    private void bindCameraViewToRoot() {
        cameraStackpane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackpane);
        });
        cameraStackpane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackpane);
        });
    }

    /**
     * Adjusts the size of the image view to fit within the specified container.
     * This method maintains the aspect ratio of the image while scaling it to fit.
     *
     * @param imageView  The ImageView containing the image to adjust.
     * @param container  The StackPane or any other container to which the image should fit.
     */
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

    /**
     * Captures an image from the camera and adds it to the gallery.
     * This method is called when the user clicks the "Take Picture" button.
     */
    private void captureImage() {
        try {
            Image image = strategy.takePhoto();
            sendToGallery(image);
            // Convert JavaFX Image to BufferedImage before adding to imagesToSave
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            imagesToSave.add(bufferedImage);
            btnConfirmation.setDisable(false);


            //TODO 1. store image in a list, could be done through the sendToGallery method.

            //TODO 2. make a save button that calls photoDAO (not directly) to save the images.

        } catch (Exception e) {
            System.err.println("Error capturing image: " + e.getMessage());
            showAlert.display("Image Capture Error", "An error occurred while capturing the image: " + e.getMessage());
        }
    }

    /**
     * Sends the captured image to the gallery for preview.
     * This method maintains a maximum of two images in the gallery.
     *
     * @param image The captured image to be added to the gallery.
     */
    private void sendToGallery(Image image) {
        if (gallery.size() == 2) {
            gallery.removeLast();
        }
        gallery.addFirst(image);

        imgPreview1.setImage((gallery.size() > 0 ? gallery.toArray(new Image[0])[0] : null));
        imgPreview2.setImage((gallery.size() > 1 ? gallery.toArray(new Image[0])[1] : null));
    }


    /**
     * Handles the action when the user clicks the "Finish Camera" button.
     * This method saves the captured images and navigates back to the operator screen.
     *
     * @param actionEvent The action event triggered by the button click.
     */
    @FXML
    public void handleFinishCamera(ActionEvent actionEvent) {
        if (imagesToSave.isEmpty()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < imagesToSave.size(); i++) {
            fileNames.add(String.valueOf(i));
        }

        try {
            photoModel.saveImageAndPath(imagesToSave, fileNames, currentUser, orderNumber);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }

        shutdownCamera();

        // Navigate back to the operator screen after saving images
        screenManager.setScreen("operator");
    }

    /**
     * Shuts down the camera and any associated resources.
     * This method is called when the user logs out or when the camera is no longer needed.
     */
    private void shutdownCamera() {
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                //TODO exception
            }
        }
    }

    /**
     * Restarts only the camera without clearing the previews.
     * This method is used when closing the preview to return to camera mode.
     */
    private void restartCameraOnly() {
        // Shutdown any existing camera resources first
        shutdownCamera();

        // Initialize the camera strategy without clearing previews
        strategy = new OpenCV();
        try {
            strategy.start();
        } catch (CameraNotFound e) {
            System.err.println("Camera not found: " + e.getMessage());
            showAlert.display("Camera Not Found", "No camera was detected. Please connect a camera and try again.");
            return;
        } catch (Exception e) {
            System.err.println("Error starting camera: " + e.getMessage());
            showAlert.display("Camera Error", "An error occurred while starting the camera: " + e.getMessage());
            return;
        }

        // Start the preview executor
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
    }


    /**
     * Handles the action when the user clicks the "Log Out" button.
     * This method shuts down the camera and navigates back to the operator screen.
     *
     * @param actionEvent The action event triggered by the button click.
     */
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
                showAlert.display("Camera Error", "An error occurred while stopping the camera: " + e.getMessage());
            }
        }

        // Navigate back to the previous screen
        screenManager.setScreen("operator");
    }

    /**
     * Opens the full preview overlay for the selected image.
     * This method is called when the user clicks on a preview image.
     *
     * @param i The index of the image to be displayed in the full preview.
     */
    private void openOverlayPreview(int i) {
        Image[] images = gallery.toArray(new Image[0]);
        if (i < images.length) {
            // Only shutdown camera if we're not already in preview mode
            if (currentPreviewIndex < 0) {
                shutdownCamera();

                // Hide buttons and show preview controls only when first entering preview mode
                imgFullPreview.setVisible(true);
                previewControls.setVisible(true);
                btnConfirmation.setVisible(false);
                btnPhotoLogout.setVisible(false);
                btnTakePicture.setVisible(false);
            }

            // Update the preview image and index
            imgFullPreview.setImage(images[i]);
            currentPreviewIndex = i;

            // Ensure the full preview is on top of the camera view
            imgFullPreview.toFront();

            adjustImage(imgFullPreview, rootPane);
        }
    }


    /**
     * Handles the action when the user clicks the "Delete Preview" button.
     * This method deletes the currently selected preview image.
     *
     * @param actionEvent The action event triggered by the button click.
     */
    @FXML
    public void handleDeletePreview(ActionEvent actionEvent) {
        deletePreview();
    }


    /**
     * Updates the preview images based on the current gallery state.
     * This method is called whenever an image is added or removed from the gallery.
     */
    private void updatePreviews() {
        Image[] images = gallery.toArray(new Image[0]);
        imgPreview1.setImage((images.length > 0 ? images[0] : null));
        imgPreview2.setImage((images.length > 1 ? images[1] : null));
    }


    /**
     * Deletes the currently selected preview image from the gallery.
     * This method removes the image from both the gallery and the imagesToSave list.
     * It also updates the previews and closes the full preview overlay.
     */
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


    /**
     * Handles the action when the user clicks the "Close Preview" button.
     * This method closes the full preview overlay and returns to camera mode.
     *
     * @param actionEvent The action event triggered by the button click.
     */
    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {
        closePreview();
    }


    /**
     * Closes the full preview overlay and returns to camera mode.
     * This method hides the preview controls and resets the current preview index.
     */
    public void closePreview() {
        imgFullPreview.setVisible(false);
        previewControls.setVisible(false);
        btnConfirmation.setVisible(true);
        btnPhotoLogout.setVisible(true);
        btnTakePicture.setVisible(true);
        currentPreviewIndex = -1;

        // Restart the camera without clearing previews
        restartCameraOnly();

        // Ensure the camera view is on top when returning to camera mode
        imgCamera.toFront();
    }


    /**
     * Handles the action when the user clicks the "Capture Image" button.
     * This method captures an image from the camera and adds it to the gallery.
     *
     * @param actionEvent The action event triggered by the button click.
     */
    @FXML
    public void handleCaptureImage(ActionEvent actionEvent) {
        captureImage();
    }
}