# Kotlin Script Runner GUI Tool

A desktop GUI application for writing, executing, and viewing Kotlin scripts in real-time.  
The application provides a side-by-side editor and output pane, supports live output and error messages, clickable navigation to errors, and indicates the script's running status.

---

## Prerequisites

- **Kotlin Interpreter** must be installed.  
- Download Kotlin compiler from the [official release](https://github.com/JetBrains/kotlin/releases/tag/v2.3.0) (scroll to Assets and download `kotlin-compiler-2.3.0.zip`).  
- Unzip the compiler anywhere on your system.  
- Update the path in `config/kotlinc-path.txt` to point to your local `kotlinc.bat` file.

Example is in `kotlinc-path.txt` content `C:\kotlin\kotlinc\bin\kotlinc.bat` :

---

## Building and Running

1. Open a terminal in the project root folder.  
2. Build the project with Maven:

```bash
mvn clean install
```
3. Run the application:
```bash
mvn javafx:run
```
4. Interact with the GUI:
   -Type or paste your Kotlin script in the Editor Pane.
   -Click the Run button to execute the script.
   -Watch the Output Pane update in real-time with standard output or error messages.
   -Click on error messages to navigate directly to the line and column where the issue occurs.


---

## Features

- **Editor Pane**:  
  Write or paste Kotlin scripts in a clean, scrollable editor (CodeArea). Supports line numbers for easy navigation.

- **Output Pane**:  
  View live output of the script as it executes. Both standard output (`stdout`) and error output (`stderr`) are displayed in real-time.

- **Run Button**:  
  Executes the script in a background thread, allowing the GUI to remain responsive.  
  - While running, the **Run** button is disabled to prevent multiple simultaneous runs.  
  - Clicking **Run** writes the script to a temporary file and executes it with `kotlinc -script <filename>.kts`.

- **Run Status Indicator**:  
  Displays the current status of the script execution:  
  - **Running...** – the script is currently executing.  
  - **Finished successfully** – the script finished with exit code `0`.  
  - **Finished with error** – the script finished with a non-zero exit code.

- **Clickable Error Navigation**:  
  Error messages in the output pane are clickable. Clicking an error moves the caret in the editor pane to the corresponding line and column.

- **Long Running Scripts**:  
  The application supports scripts that take a long time to execute without freezing the GUI, thanks to background threads for execution and live output capture.

- **Keyword Highlighting (Partial)**:  
  Keywords are loaded from `config/keywords.txt`. Styling (coloring) does not currently apply fully but keywords are recognized internally.

---
## Usage Examples

-Successful Run:
Script executes without errors. Status shows Finished successfully. Output pane shows script results.

-Failed Run:
Script contains errors. Status shows Finished with error. Output pane shows detailed error messages with line and column numbers. Clicking an error moves the caret to the exact location in the editor.

-Running Script:
While the script is executing, the Run button is disabled, and the status shows Running.... Live output continues to populate as the script runs.

---

## Additional Information

 - **Script files are saved in the archive/ folder with timestamped names.**
 - **Exit files are renamed automatically with _SUCCESS or _FAILED suffixes based on the exit code.**
 - **The program uses JavaFX and RichTextFX for the GUI.**
 - **Ensure kotlinc.bat path is correctly set before running, otherwise the execution will fail.**
 - **You can modify or extend the keywords list in config/keywords.txt for future highlighting improvements.**
