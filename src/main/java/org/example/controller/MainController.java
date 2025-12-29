package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    /// KEYWORDS LOGIC
//    private Set<String> keywords;
    private final List<ErrorLocation> errorLocations = new ArrayList<>();

    private static class ErrorLocation {
        int lineNumber;
        int colNumber;
        int startOffset;
        int endOffset;

    }

    @FXML
    public void initialize() {
        /// KEYWORDS LOGIC
//        keywords = loadKeywords();
//        System.out.println("Keywords loaded: " + keywords);

        /// KEYWORDS LOGIC
        scriptArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13pt;");
        scriptArea.setWrapText(false);
        scriptArea.setParagraphGraphicFactory(LineNumberFactory.get(scriptArea));

        /// KEYWORDS LOGIC
        outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13pt;");
        outputArea.setWrapText(true);
        outputArea.setEditable(false);

        outputArea.setOnMouseClicked(e -> {
            int caret = outputArea.getCaretPosition();
            for (ErrorLocation loc : errorLocations) {
                if (caret >= loc.startOffset && caret <= loc.endOffset) {
                    scriptArea.moveTo(loc.lineNumber - 1, loc.colNumber - 1);
                    scriptArea.requestFocus();
                    break;
                }
            }
        });


        setStatus(ScriptStatus.WAITING);
    }

    @FXML
    private Button runButton;

    @FXML
    private CodeArea scriptArea;

    @FXML
    private CodeArea outputArea;

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
            appendLine("script.kts created\n","default");
        } catch (IOException e) {
            appendLine("Failed to write script.kts:" + e.toString(), "error"); /// OVDE IZMENA //outputArea.appendText("Failed to write script.kts:" + e.toString() + "\n");
            return;
        }

        // BACKGROUND THREAD (main THRED)
        new Thread(() -> {
            try {
                String kotlincPath = loadKotlincPath();

                ProcessBuilder pb = new ProcessBuilder(
                        kotlincPath,
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
                                    appendLine(finalLine, "stdout")
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
                            Platform.runLater(() -> appendErrorLine("ERROR: " + finalLine));
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

    private String loadKotlincPath() {
        try {
            Path path = Paths.get("config", "kotlinc-path.txt");
            return Files.readString(path).trim();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read kotlinc-path.txt", e);
        }
    }


    private void appendLine(String text, String type) {
        int start = outputArea.getLength();
        //Platform.runLater(() -> outputArea.appendText(text + "\n"));
        outputArea.appendText(text + "\n");
        int end = outputArea.getLength();

        /// KEYWORDS LOGIC
        switch (type) {
            case "error" -> outputArea.setStyle(start, end, Collections.singleton("-fx-fill: red; -fx-font-weight: bold;"));
            case "success" -> outputArea.setStyle(start, end, Collections.singleton("-fx-fill: green;"));
            default -> outputArea.setStyle(start, end, Collections.singleton("-fx-fill: black;"));
        }

        if (type.equals("error")) {
            Matcher matcher = Pattern.compile(".*:(\\d+):(\\d+):.*").matcher(text);
            if (matcher.find()) {
                int line = Integer.parseInt(matcher.group(1));
                int col  = Integer.parseInt(matcher.group(2));

                ErrorLocation loc = new ErrorLocation();
                loc.lineNumber = line;
                loc.colNumber  = col;
                loc.startOffset = start;
                loc.endOffset   = end;
                errorLocations.add(loc);
            }
        }
        outputArea.moveTo(outputArea.getLength());
        outputArea.requestFollowCaret();
    }

    /// KEYWORDS LOGIC
//    private Set<String> loadKeywords() {
//        Set<String> keywords = new HashSet<>();
//        try {
//            Files.lines(Paths.get("config/keywords.txt"))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .forEach(keywords::add);
//        } catch (IOException e) {
//            appendLine("Failed to load keywords: " + e.getMessage(), "error");
//        }
//        return keywords;
//    }

    private void appendErrorLine(String line) {
        int start = outputArea.getLength();
        outputArea.appendText(line + "\n");
        int end = outputArea.getLength();

        Pattern p = Pattern.compile("ERROR: .*:(\\d+):(\\d+): error:.*");
        Matcher m = p.matcher(line);
        if (m.matches()) {
            int lineNumber = Integer.parseInt(m.group(1));
            int column = Integer.parseInt(m.group(2));

            /// KEYWORDS LOGIC
//            outputArea.setStyle(start, end, Collections.singleton("-fx-fill: red; -fx-underline: true;"));


            ErrorLocation loc = new ErrorLocation();
            loc.lineNumber = lineNumber;
            loc.colNumber = column;
            loc.startOffset = start;
            loc.endOffset = end;
            errorLocations.add(loc);
        } else {

            /// KEYWORDS LOGIC
//            outputArea.setStyle(start, end, Collections.singleton("-fx-fill: black;"));
        }

    }
}




