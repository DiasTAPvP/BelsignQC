package com.belman.belsignqc;

import com.belman.belsignqc.BLL.Util.ScreenManager;
import com.belman.belsignqc.GUI.Controller.BaseController;
import javafx.application.Application;
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

        //Load CSS file
        String css = getClass().getResource("style.css").toExternalForm();

        // Load all the screens
        loadScreen("login", "loginScreen.fxml");
        loadScreen("operator", "operatorScreen.fxml");
        loadScreen("admin", "adminScreen.fxml");
        loadScreen("qa", "qaScreen.fxml");
        loadScreen("createuser", "createScreen.fxml");
        loadScreen("camera", "cameraScreen.fxml");

        // Set initial screen to login
        screenManager.setScreen("login");

        // Create scene with the screen manager
        Scene scene = new Scene(screenManager, 1200, 700);
        scene.getStylesheets().add(css); //Apply the CSS file to the scene

        stage.setTitle("Belsign™");
        stage.setScene(scene);
        stage.show();
    }

    private void loadScreen(String name, String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        Parent loadScreen = loader.load();

        // If controller extends BaseController, set the screen manager and register it
        Object controller = loader.getController();
        if (controller instanceof BaseController) {
            BaseController baseController = (BaseController) controller;
            baseController.setScreenManager(screenManager);
            screenManager.addController(name, baseController);
        }

        screenManager.addScreen(name, loadScreen);
    }

    public static void main(String[] args) {
        launch();
    }
}
