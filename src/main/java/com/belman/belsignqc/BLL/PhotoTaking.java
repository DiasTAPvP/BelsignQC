package com.belman.belsignqc.BLL;

import javafx.scene.image.Image;

/**
 * Interface for taking photos using a camera.
 * Provides methods to start and stop the camera, and to take a photo.
 * This interface is used by the OpenCV class to implement camera functionality.
 */
public interface PhotoTaking {

    void start() throws Exception;

    void stop() throws Exception;

    Image takePhoto() throws Exception;
}