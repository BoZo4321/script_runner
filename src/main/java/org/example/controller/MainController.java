package org.example.controller;

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
       // System.out.println(scriptArea.getText());

        outputArea.appendText("Running...\n");
        statusLabel.setText("RUNNING");
    }
}
