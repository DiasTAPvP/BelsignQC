module com.belman.belsignqc {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.belman.belsignqc to javafx.fxml;
    exports com.belman.belsignqc;
}