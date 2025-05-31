package com.belman.belsignqc;

import com.belman.belsignqc.BLL.Util.ScreenManager;
import com.belman.belsignqc.GUI.Controller.BaseController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BelsignApplication extends Application {

    private ScreenManager screenManager;

    @Override
    public void start(Stage stage) throws IOException {
        screenManager = new ScreenManager();
        String css = getClass().getResource("style.css").toExternalForm();

        // Only load the login screen initially
        loadScreen("login", "loginScreen.fxml");
        screenManager.setScreen("login");

        // Load other screens in background
        // This ensures that the UI for logging in is responsive while loading other screens
        // Also helps start the application more quickly
        Platform.runLater(() -> {
            loadRemainingScreens();
        });

        Scene scene = new Scene(screenManager, 1200, 700);
        scene.getStylesheets().add(css);
        stage.setTitle("Belsign™");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads a screen from the specified FXML resource file.
     * The screen is added to the ScreenManager with the given name.
     *
     * @param name     The name of the screen to be added.
     * @param resource The FXML resource file for the screen.
     * @throws IOException If there is an error loading the FXML file.
     */
    private void loadScreen(String name, String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        Parent loadScreen = loader.load();

        // Get the controller
        Object controller = loader.getController();

        // If controller extends BaseController, set the screen manager
        if (controller instanceof BaseController) {
            BaseController baseController = (BaseController) controller;
            baseController.setScreenManager(screenManager);
            // Add screen with all three required parameters
            screenManager.addScreen(name, loadScreen, baseController);
        } else {
            System.err.println("Warning: Controller for " + name + " does not extend BaseController");
        }
    }

    /**
     * Loads the remaining screens in the application.
     * This method is called after the initial login screen is loaded.
     * It loads screens for operator, admin, QA, create user, and camera functionalities.
     */
    private void loadRemainingScreens() {
        // List of screens and their corresponding FXML resources
        String[] screens = {"operator", "admin", "qa", "createuser", "camera", "auditLog"};
        String[] resources = {"operatorScreen.fxml", "adminScreen.fxml", "qaScreen.fxml", "createScreen.fxml", "cameraScreen.fxml", "auditScreen.fxml"};

        for (int i = 0; i < screens.length; i++) {
            try {
                loadScreen(screens[i], resources[i]);
            } catch (IOException e) {
                System.err.println("Error loading screen: " + screens[i]);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}