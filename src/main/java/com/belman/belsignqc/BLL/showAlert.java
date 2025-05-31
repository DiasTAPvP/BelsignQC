package com.belman.belsignqc.BLL;

import javafx.scene.control.Alert;

public class showAlert {

    /**
     * Displays an error alert with the given title and content.
     *
     * @param title   The title of the alert.
     * @param content The content of the alert.
     */
    public static void display(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}