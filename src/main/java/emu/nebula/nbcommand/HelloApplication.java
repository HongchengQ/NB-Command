package emu.nebula.nbcommand;

import emu.nebula.nbcommand.service.I18nManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 初始化默认语言
        I18nManager i18n = I18nManager.getInstance();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 625);
        stage.setTitle(i18n.getString("label.version"));
        stage.setScene(scene);
        stage.show();
    }
}