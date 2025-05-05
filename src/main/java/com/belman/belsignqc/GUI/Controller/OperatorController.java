package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class OperatorController extends BaseController {

    @FXML private ImageView operatorLogoutButton;

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");

        //Make it forget who is logged in later too
    }
}