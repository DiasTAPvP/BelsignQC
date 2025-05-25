package com.belman.belsignqc.BLL;

import javafx.scene.control.Alert;

public class showAlert {
//
    public static void display(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}