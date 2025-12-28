package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.*;

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
                    String scriptContent = scriptArea.getText();
                    File file = new File("script.kts");
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(scriptContent);
                    }

                    // Simulating a job in progress
                    Thread.sleep(500);

                    // Povratak u UI thread da se updatuje status
                    Platform.runLater(() -> {
                        outputArea.appendText("File 'script.kts' created successfully!\n");
                        outputArea.appendText("Done.\n");
                        statusLabel.setText("IDLE");
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        outputArea.appendText("Error writing file: " + e.getMessage() + "\n");
                        statusLabel.setText("Error");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> statusLabel.setText("Error"));
                }
            }).start();


        statusLabel.setText("Running...");
        outputArea.clear();
        outputArea.appendText("Running...\n");

    }
}
