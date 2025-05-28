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
        Platform.runLater(() -> {
            loadRemainingScreens();
        });

        Scene scene = new Scene(screenManager, 1200, 700);
        scene.getStylesheets().add(css);
        stage.setTitle("Belsign™");
        stage.setScene(scene);
        stage.show();
    }

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
            // Handle case where controller doesn't extend BaseController
            // TODO exception handling or logging
            System.err.println("Warning: Controller for " + name + " does not extend BaseController");
        }
    }

    private void loadRemainingScreens() {
        String[] screens = {"operator", "admin", "qa", "createuser", "camera"};
        String[] resources = {"operatorScreen.fxml", "adminScreen.fxml", "qaScreen.fxml", "createScreen.fxml", "cameraScreen.fxml"};

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