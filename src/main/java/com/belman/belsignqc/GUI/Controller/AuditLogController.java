package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.DAL.DAO.PhotoDAO;
import com.belman.belsignqc.DAL.DAO.UserDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class AuditLogController extends BaseController {

    @FXML
    private Label AuditOrderLabel;  //Make sure FXML id matches

    @FXML
    private TextArea AuditTextArea;

    private UserDAO userDAO;

    @Override
    public void receiveData(Object data) {
        if (data instanceof String orderId) {
            AuditOrderLabel.setText("Audit Log For Order: " + orderId);
            loadLog(orderId);
    }
}

private void loadLog(String orderId) {
        try {
            PhotoDAO photoDAO = new PhotoDAO();

            if (userDAO == null) {
                userDAO = new UserDAO();
            }
            ObservableList<Photos> photos = photoDAO.getImagesForOrder(orderId);
            if (photos.isEmpty()) {
                AuditTextArea.setText("No photos have been uploaded for this order yet.");
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            StringBuilder sb = new StringBuilder();

            for (Photos p : photos) {
                String user = userDAO.getUsernameById(p.getUserID());
                sb.append(String.format("[%s] Photo uploaded by %s (%s)%n", p.getUploadTime().toLocalDateTime().format(fmt), user, Paths.get(p.getFilepath()).getFileName()));
            }

            AuditTextArea.setText(sb.toString());

        } catch (Exception e) {
            AuditTextArea.setText("Error reading audit data.");
            e.printStackTrace();
        }
}

    @FXML
    private void onAuditReturnButton() {
    screenManager.setScreen("admin");
    }
}