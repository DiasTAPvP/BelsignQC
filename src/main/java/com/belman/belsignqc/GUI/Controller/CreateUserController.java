package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class CreateUserController extends BaseController {

    @FXML private ImageView createReturnButton;

    @FXML
    private void handleReturn() {
        //Implement wiping the fields after returning
        screenManager.setScreen("admin");
    }

}
