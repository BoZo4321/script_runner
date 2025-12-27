package org.example;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
        );
        stage.setTitle("Script Runner");
        stage.setScene(new Scene(loader.load(), 900, 600));
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}