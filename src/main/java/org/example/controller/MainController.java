package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class MainController {

    @FXML
    private TextArea scriptArea;

    @FXML
    private TextArea outputArea;

    @FXML
    private Label statusLabel;

    @FXML
    private void onRun(){

        statusLabel.setText("Running...");
        outputArea.appendText("Running...\n");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // 1 sekunda

                // When Run is complited
                Platform.runLater(() -> {
                    outputArea.appendText("Done.\n");
                    statusLabel.setText("Wating for user response");
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Error"));
            }
        }).start();


    }
}
