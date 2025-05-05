package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class QAController extends BaseController{

    @FXML
    private ImageView qaReturnButton;


    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");
    }
}
