package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;

public class OperatorController extends BaseController {

    @FXML
    private void onLogoutPressed() {
        screenManager.setScreen("login");

        //Make it forget who is logged in later too
    }
}