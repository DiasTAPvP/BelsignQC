module com.belman.belsignqc {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires bcrypt;
    requires opencv;
    requires java.desktop;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires java.naming;

    opens com.belman.belsignqc to javafx.fxml;
    exports com.belman.belsignqc;
    opens com.belman.belsignqc.BLL to javafx.base;
    opens com.belman.belsignqc.GUI.Model to javafx.base;
    //exports com.belman.belsignqc.GUI;
    //opens com.belman.belsignqc.GUI to javafx.fxml;
    exports com.belman.belsignqc.GUI.Controller;
    opens com.belman.belsignqc.GUI.Controller to javafx.fxml;

}