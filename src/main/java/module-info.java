module com.belman.belsignqc {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires bcrypt;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;

    opens com.belman.belsignqc to javafx.fxml;
    exports com.belman.belsignqc;
    opens com.belman.belsignqc.BLL to javafx.base;
    opens com.belman.belsignqc.GUI.Model to javafx.base;
    //exports com.belman.belsignqc.GUI;
    //opens com.belman.belsignqc.GUI to javafx.fxml;
    exports com.belman.belsignqc.GUI.Controller;
    opens com.belman.belsignqc.GUI.Controller to javafx.fxml;

}