package com.belman.belsignqc.BLL;

import com.belman.belsignqc.BLL.Exceptions.CameraNotFound;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;

public class OpenCV implements PhotoTaking {

    private VideoCapture camera;
//
    @Override
    /* Starts the camera and sets the resolution to 1280x720.
     * @throws CameraNotFound if the camera is not found.
     */
    public void start() {
        try {
            nu.pattern.OpenCV.loadLocally();

            camera = new VideoCapture(0);
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);

            if (!camera.isOpened()) {
                throw new CameraNotFound();
            }
        } catch (CameraNotFound e) {
            showAlert.display("Camera Error", e.getMessage());
        } catch (Exception e) {
            showAlert.display("Unexpected Error", "An unexpected error occurred while starting the camera.");
        }
    }


    @Override
    /* Stops the camera and releases the resources.
     */
    public void stop() {
        if (camera != null) {
            camera.release();
        }
    }

    @Override
    /* Takes a photo using the camera and returns it as an Image.
     * @throws Exception if the frame is empty.
     */
        public Image takePhoto () {
            try {
                Mat frame = new Mat();
                camera.read(frame);
                if (frame.empty()) {
                    throw new Exception("Frame empty");
                }

                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".png", frame, buffer);
                return new Image(new ByteArrayInputStream(buffer.toArray()));
            } catch (Exception e) {
                showAlert.display("Photo Error", e.getMessage());
                return null;
            }
        }
    }
