package emu.nebula.nbcommand;

import javafx.application.Application;

public class Launcher {
    public static String version = "v1.2.1";
    public static void main(String[] args) {

        System.setProperty("javafx.platform", "desktop");
        Application.launch(HelloApplication.class, args);
    }
}