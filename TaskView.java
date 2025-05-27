import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

//Main code editor: Malulu

public class TaskView {

    String filename = " ";

    public StackPane getView(String username) {

        filename = username + "Budget.txt";
        
        switch (fileCheck(filename)) {
            case 1:
            	writeInitial();
            	showAlert(AlertType.INFORMATION, "Initial entries written.\nInitial budgets for each entries are set to 20,000");
            
        }

        StackPane root = new StackPane();
        
        HBox button = new HBox(10);
        button.setAlignment(Pos.CENTER);
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 30, 20, 30));

        // Title and Budget input fields
        TextField titleInput = new TextField();
        titleInput.setPromptText("輸入記帳科目");

        TextField budgetInput = new TextField();
        budgetInput.setPromptText("輸入該科目的預算:");
        
        // Output area
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setPrefRowCount(10);
        displayArea.setPrefColumnCount(30);
        updateDisplay(displayArea);

        // Save button
        Button saveButton = new Button("Add Entry");
        
        //Edit button
        Button editButton = new Button("Edit");

        saveButton.setOnAction(e -> {
            String title = titleInput.getText().trim();
            String budget = budgetInput.getText().trim();

            if (title.isEmpty() || budget.isEmpty()) {
                showAlert(AlertType.WARNING, "Both fields must be filled.");
                return;
            }
            if(Integer.valueOf(budget) < 0) {
            	showAlert(AlertType.WARNING, "Budget must be more than 0.");
                return;
            }

            if (isDuplicateTitle(title)) {
                showAlert(AlertType.ERROR, "Title already exists.");
            } else {
                writeFile(title, budget);
                updateDisplay(displayArea);
                showAlert(AlertType.INFORMATION, "Entry saved.");
                titleInput.clear();
                budgetInput.clear();
            }
        });
        
        editButton.setOnAction(e -> {
        	String title = titleInput.getText().trim();
            String budget = budgetInput.getText().trim();
            
            if (title.isEmpty() || budget.isEmpty()) {
                showAlert(AlertType.WARNING, "Both fields must be filled. (To delete, set budget to 0.");
                return;
            }
            if(Integer.valueOf(budget) < 0) {
            	showAlert(AlertType.WARNING, "Budget must be more than 0.");
                return;
            }
            
            if(isDuplicateTitle(title)) {
            	deleteTitle(title);
            	if(!budget.equals("0")) {
                    writeFile(title, budget);
                    showAlert(AlertType.INFORMATION, "Entry edited and saved.");
            	}else {
                    showAlert(AlertType.INFORMATION, "Entry deleted and saved.");
            	}
                updateDisplay(displayArea);
                titleInput.clear();
                budgetInput.clear();
            }
        });
        button.getChildren().addAll(saveButton, editButton);
        content.getChildren().addAll(titleInput, budgetInput, button, displayArea);
        root.getChildren().add(content);
        return root;
    }

    private int fileCheck(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Save file not found. New file created.");
                    return 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("Save file found.");
            return 0;
        }
        return 2;
    }

    private void writeFile(String title, String budget) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(title + "   " + budget + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDisplay(TextArea area) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        area.setText(content.toString());
    }
    
    private void deleteTitle(String titleToDelete) {
        File originalFile = new File(filename);
        File tempFile = new File("temp_" + filename);
        boolean found = false;
        try (
            BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip header, always keep it
                if (line.trim().equalsIgnoreCase("Title   Budget")) {
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                // Check if line starts with the title
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(titleToDelete)) {
                    found = true; // Skip this line (do not write it)
                    continue;
                }

                // Keep the rest
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Replace original file with temp file
        if (!originalFile.delete() || !tempFile.renameTo(originalFile)) {
            showAlert(AlertType.ERROR, "Failed to update the file.");
        }

        if (!found) {
            showAlert(AlertType.WARNING, "Title not found.");
        }
    }

    
    private void writeInitial() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Title   Budget\n食物  20000\n生活用品  20000\n衣服  20000\n通勤  20000\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicateTitle(String title) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(title)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

