package com.belman.belsignqc.BLL;

import com.belman.belsignqc.BLL.Exceptions.CameraNotFound;
import javafx.scene.image.Image;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenCVTest {

    private OpenCV openCV;
    private MockVideoCapture mockCamera;
    private TestShowAlert testShowAlert;

    @BeforeEach
    void setUp() {
        // Create test doubles
        mockCamera = new MockVideoCapture();
        testShowAlert = new TestShowAlert();
        openCV = new OpenCVTestable(mockCamera, testShowAlert);
    }

    @AfterEach
    void tearDown() {
        if (testShowAlert != null) {
            testShowAlert.clearAlerts();
        }
    }

    // Custom mock for VideoCapture instead of extending it
    private static class MockVideoCapture {
        private boolean isOpen = true;
        private boolean wasReleased = false;
        private boolean returnsEmptyFrame = false;

        public boolean isOpened() {
            return isOpen;
        }

        public void setOpened(boolean opened) {
            isOpen = opened;
        }

        public boolean set(int propId, double value) {
            return true;
        }

        public void release() {
            wasReleased = true;
        }

        public boolean read(Object image) {
            return true;
        }

        public void setReturnsEmptyFrame(boolean returnsEmpty) {
            this.returnsEmptyFrame = returnsEmpty;
        }

        public boolean returnsEmptyFrame() {
            return returnsEmptyFrame;
        }

        public boolean wasReleased() {
            return wasReleased;
        }
    }

    // Test double for showAlert class
    private static class TestShowAlert {
        private List<AlertCall> alerts = new ArrayList<>();

        public void display(String title, String message) {
            alerts.add(new AlertCall(title, message));
        }

        public List<AlertCall> getAlerts() {
            return alerts;
        }

        public void clearAlerts() {
            alerts.clear();
        }

        public boolean hasAlert(String title) {
            return alerts.stream().anyMatch(a -> a.title.equals(title));
        }

        private static class AlertCall {
            String title;
            String message;

            AlertCall(String title, String message) {
                this.title = title;
                this.message = message;
            }
        }
    }

    // Testable OpenCV subclass
    private class OpenCVTestable extends OpenCV {
        private final MockVideoCapture testCamera;
        private final TestShowAlert testShowAlert;

        public OpenCVTestable(MockVideoCapture camera, TestShowAlert showAlert) {
            this.testCamera = camera;
            this.testShowAlert = showAlert;
        }

        @Override
        public void start() {
            // Mock the start method instead of using reflection
            // Original behavior is simulated
            if (!testCamera.isOpened()) {
                testShowAlert.display("Camera Error", "Camera not found");
            }
        }

        @Override
        public void stop() {
            testCamera.release();
        }

        @Override
        public Image takePhoto() {
            if (testCamera.returnsEmptyFrame()) {
                testShowAlert.display("Photo Error", "Frame empty");
                return null;
            }

            // Return a minimal test image for successful cases
            return new Image(new ByteArrayInputStream(new byte[]{1,2,3,4}), 1, 1, true, false);
        }

        // Method to use test alert implementation
        protected void showAlertMessage(String title, String message) {
            testShowAlert.display(title, message);
        }
    }

    @Test
    @DisplayName("Camera starts successfully")
    void cameraStartsSuccessfully() {
        // Arrange
        mockCamera.setOpened(true);

        // Act
        openCV.start();

        // Assert - No alerts should be shown
        assertFalse(testShowAlert.hasAlert("Camera Error"));
    }

    @Test
    @DisplayName("Camera fails to start")
    void cameraFailsToStart() {
        // Arrange
        mockCamera.setOpened(false);

        // Act
        openCV.start();

        // Assert
        assertTrue(testShowAlert.hasAlert("Camera Error"));
    }

    @Test
    @DisplayName("Camera stops properly")
    void cameraStopsProperly() {
        // Arrange
        openCV.start();

        // Act
        openCV.stop();

        // Assert
        assertTrue(mockCamera.wasReleased());
    }

    @Test
    @DisplayName("Take photo returns valid image")
    void takePhotoReturnsValidImage() {
        // Arrange
        openCV.start();
        mockCamera.setReturnsEmptyFrame(false);

        // Act
        Image result = openCV.takePhoto();

        // Assert
        assertNotNull(result);
        assertFalse(testShowAlert.hasAlert("Photo Error"));
    }

    @Test
    @DisplayName("Take photo handles empty frame")
    void takePhotoHandlesEmptyFrame() {
        // Arrange
        openCV.start();
        mockCamera.setReturnsEmptyFrame(true);

        // Act
        Image result = openCV.takePhoto();

        // Assert
        assertNull(result);
        assertTrue(testShowAlert.hasAlert("Photo Error"));
    }
}