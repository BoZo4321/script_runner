package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML
    public void initialize() {
        setStatus(ScriptStatus.WAITING);
    }

    @FXML
    private Button runButton;

    @FXML
    private TextArea scriptArea;

    @FXML
    private TextArea outputArea;

    @FXML
    private Label statusLabel;

    @FXML
    private void onRun() {

        outputArea.clear();
        setStatus(ScriptStatus.RUNNING);

        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yy_MM_dd_HH_mm_ss");
        String timestamp = LocalDateTime.now().format(formatDate);

        Path scriptPath = Paths.get("archive\\script" + timestamp + ".kts");

        try {
            Files.writeString(scriptPath, scriptArea.getText());
            outputArea.appendText("script.kts written successfully\n");
        } catch (IOException e) {
            outputArea.appendText("Failed to write script.kts:" + e.toString() + "\n");
            return;
        }

        // BACKGROUND THREAD (main THRED)
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "C:\\kotlin\\kotlinc\\bin\\kotlinc.bat",
                        "-script",
                        scriptPath.toAbsolutePath().toString()
                );

                Process process = pb.start();

                // STDOUT THREAD
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() ->
                                    outputArea.appendText(finalLine + "\n")
                            );
                        }
                    } catch (IOException ignored) {
                    }
                }).start();

                // STDERR THREAD
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> {
                                outputArea.appendText("ERROR: " + finalLine + "\n");

                                /// THIS NEEDS A FIX, WORKS BUT PAINTS WHOLE TEXTAREA AND DOEST'T RESET ///
                                outputArea.setStyle("-fx-text-fill: red;");
                            });
                        }
                    } catch (IOException ignored) {
                    }
                }).start();

                int exitCode = process.waitFor();

                //Renaming of exit file (little slow, don't have time to optimaze it) NOT A CORE FUNCTION
                String suffix = exitCode == 0 ? "_SUCCESS" : "_FAILED";
                Files.move(
                        scriptPath,
                        Paths.get(scriptPath.toString().replace(".kts", suffix + ".kts")),
                        StandardCopyOption.REPLACE_EXISTING
                );


                setStatus(exitCode == 0
                        ? ScriptStatus.SUCCESS
                        : ScriptStatus.ERROR
                );

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() ->
                        outputArea.appendText("Error running script: " + e.getMessage() + "\n")
                );
            }
        }).start();
    }

    private ScriptStatus currentStatus = ScriptStatus.WAITING;

    private void setStatus(ScriptStatus status) {
        currentStatus = status;

        Platform.runLater(() -> {

            statusLabel.getStyleClass().removeAll(
                    "status-waiting",
                    "status-running",
                    "status-success",
                    "status-error"
            );

            switch (status) {
                case WAITING:
                    statusLabel.setText("Waiting for user action");
                    statusLabel.getStyleClass().add("status-waiting");
                    runButton.setDisable(false);
                    break;

                case RUNNING:
                    statusLabel.setText("Running...");
                    statusLabel.getStyleClass().add("status-running");
                    runButton.setDisable(true);
                    break;

                case SUCCESS:
                    statusLabel.setText("Finished successfully");
                    statusLabel.getStyleClass().add("status-success");
                    runButton.setDisable(false);
                    break;

                case ERROR:
                    statusLabel.setText("Finished with error");
                    statusLabel.getStyleClass().add("status-error");
                    runButton.setDisable(false);
                    break;
            }
        });
    }

    /// TESTING A JUMP TO FEATURE
    @FXML
    public void testRun(ActionEvent actionEvent) {
        scriptArea.requestFocus();
        scriptArea.positionCaret(3);
    }
}
