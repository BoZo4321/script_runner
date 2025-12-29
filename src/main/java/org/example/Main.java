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
        Scene scene = new Scene(loader.load(), 900, 600);

        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        stage.setTitle("Script Runner");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}