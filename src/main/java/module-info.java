module emu.nebula.nbcommand {
    requires javafx.controls;
    requires javafx.fxml;
    requires ch.qos.logback.classic;
    requires org.slf4j;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens emu.nebula.nbcommand to javafx.fxml;
    opens emu.nebula.nbcommand.controller to javafx.fxml;
    opens emu.nebula.nbcommand.service to javafx.fxml;
    opens emu.nebula.nbcommand.service.command to javafx.fxml;

    exports emu.nebula.nbcommand;
    exports emu.nebula.nbcommand.controller;
    exports emu.nebula.nbcommand.model;
    exports emu.nebula.nbcommand.model.command;
    exports emu.nebula.nbcommand.service;
    exports emu.nebula.nbcommand.service.command;
    exports emu.nebula.nbcommand.ui;
}