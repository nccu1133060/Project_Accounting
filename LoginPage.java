import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class LoginPage {

	private final String FIELD_DET = "XXXX";
	private final String DAYS_DET = "X";
	private final String WEEK_DET = "XX";
	
    private TextField tfUserName;
    private PasswordField tfPassword;
    private Button btnLogin, btnEnroll, btnClearFile, btnDevLogin;
    private String fileName = "UserNames.txt";

    public void createLoginPage(Stage stage) {
        fileCheck(fileName);

        VBox layout = new VBox(10);
        VBox button = new VBox(10);
        tfUserName = new TextField();
        tfUserName.setPromptText("使用者名稱");

        tfPassword = new PasswordField();
        tfPassword.setPromptText("輸入密碼");

        btnEnroll = new Button("註冊");
        btnLogin = new Button("登入");
        btnClearFile = new Button("清除檔案並關閉程式(按下前請三思)");
        btnClearFile.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #FFFFFF;");
        btnDevLogin = new Button("DevLogin");

        btnLogin.setOnAction(e -> handleLogin(stage));
        btnEnroll.setOnAction(e -> handleEnroll());
        btnClearFile.setOnAction(e -> handleClearFile());
        btnDevLogin.setOnAction(e -> handleDevLogin(stage));
        
        button.getChildren().addAll(btnDevLogin, btnLogin, btnEnroll, btnClearFile);
        layout.getChildren().addAll(tfUserName, tfPassword, button);

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("記帳軟體登入");
        stage.setScene(scene);
        stage.show();
    }
    
    //Checks if file exists, if not, create a new file.
    private void fileCheck(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                	try (FileWriter writer = new FileWriter(fileName, true)) {  // true = append
                        writer.write("malu" + FIELD_DET + "0000" + FIELD_DET + "0" + FIELD_DET +
                                     "0" + WEEK_DET + FIELD_DET + "0" + FIELD_DET + "0" + "\n");
                        showSuccess("Enrollment successful!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Error enrolling user");
                    System.out.println("Save file not found. New file created.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println("Save file found.");
        }
    }
    
    //The login process after the user presses the login button
    private void handleLogin(Stage stage) {
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        int totalLoggedInDays = 0;

        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("使用者名稱或密碼不得為空!");
            return;
        }

        boolean userFound = false;
        String storedP = null;
        int weekLogin = 0;
        List<Integer> loginDays = new ArrayList<>();
        int recordedWeek = -1;
        List<String> allLines = new ArrayList<>();
        String matchedLine = null;
        /*
         * FORMAT: name XXXX password XXXX login days in a week XXXX current week XXXX total logged in days
         */
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                allLines.add(line);
                String[] parts = line.split(FIELD_DET);

                if (parts.length >= 6) {
                    String storedU = parts[0];
                    if (storedU.equals(inputU)) {
                        userFound = true;
                        storedP = parts[1];
                        weekLogin = Integer.parseInt(parts[2]);
                        String loginDaysRaw = parts[3];

                        // Parse login days
                        String[] loginDayParts = loginDaysRaw.split(WEEK_DET);
                        String recordedDaysStr = loginDayParts.length > 0 ? loginDayParts[0] : "";
                        String[] recordedDaysArray = recordedDaysStr.isEmpty() ? new String[0] : recordedDaysStr.split(DAYS_DET);

                        for (String d : recordedDaysArray) {
                            try {
                                loginDays.add(Integer.parseInt(d));
                            } catch (NumberFormatException ignored) {}
                        }

                        try {
                            recordedWeek = Integer.parseInt(parts[4]);
                        } catch (NumberFormatException ignored) {}

                        try {
                            totalLoggedInDays = Integer.parseInt(parts[5]);
                        } catch (NumberFormatException ignored) {}

                        matchedLine = line;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("讀檔時錯誤!");
            return;
        }

        if (!userFound) {
            showError("找不到使用者名稱!");
            return;
        }

        if (!inputP.equals(storedP)) {
            showError("密碼錯誤!");
            return;
        }

        // Password is correct: update login data
        LocalDate today = LocalDate.now();
        int currentDayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());

        if (recordedWeek != currentWeek) {
            loginDays.clear();
            loginDays.add(currentDayOfWeek);
            if (currentDayOfWeek == 7 || weekLogin > 7) {
            	weekLogin = 1;
            }else {
            	weekLogin++;
            }
            totalLoggedInDays++;  // First login of a new week, so it's a new day
        } else if (!loginDays.contains(currentDayOfWeek)) {
            loginDays.add(currentDayOfWeek);
            weekLogin++;
            totalLoggedInDays++;  // First login of the day in current week
        }

        Collections.sort(loginDays);
        StringBuilder sb = new StringBuilder();
        for (int day : loginDays) {
            sb.append(day).append(DAYS_DET);
        }
        sb.append(FIELD_DET); // Ending "XXXX"

        String updatedLine = inputU + FIELD_DET + storedP + FIELD_DET + weekLogin + FIELD_DET +
                sb + FIELD_DET + currentWeek + FIELD_DET + totalLoggedInDays;

        // Rebuild file with updated line
        List<String> updatedLines = new ArrayList<>();
        for (String line : allLines) {
            if (line.equals(matchedLine)) {
                updatedLines.add(updatedLine + "\n");
            } else {
                updatedLines.add(line + "\n");
            }
        }

        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (String updated : updatedLines) {
                writer.write(updated + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("更新登入資料時錯誤!");
            return;
        }

        Integer[] loginDaysArray = loginDays.toArray(new Integer[0]);
        loginSuccess(inputU, stage, loginDaysArray, totalLoggedInDays);
    }

    //Passes the data after success login
    private void loginSuccess(String name, Stage stage, Integer[] loginDaysArray, int totalLoggedInDays) {
        Project_Accounting app = new Project_Accounting();
        app.startMainApplication(stage, name, loginDaysArray, totalLoggedInDays);
    }

    //Identical to normal login only with the name and password set to mine
    private void handleDevLogin(Stage stage) {
        String inputU = "malu";
        String inputP = "0000";
        int totalLoggedInDays = 0;

        boolean userFound = false;
        String storedP = null;
        int weekLogin = 0;
        List<Integer> loginDays = new ArrayList<>();
        int recordedWeek = -1;
        List<String> allLines = new ArrayList<>();
        String matchedLine = null;
        /*
         * FORMAT: name XXXX password XXXX login days in a week XXXX current week XXXX total logged in days
         */
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                allLines.add(line);
                String[] parts = line.split(FIELD_DET);

                if (parts.length >= 6) {
                    String storedU = parts[0];
                    if (storedU.equals(inputU)) {
                        userFound = true;
                        storedP = parts[1];
                        weekLogin = Integer.parseInt(parts[2]);
                        String loginDaysRaw = parts[3];

                        // Parse login days
                        String[] loginDayParts = loginDaysRaw.split(WEEK_DET);
                        String recordedDaysStr = loginDayParts.length > 0 ? loginDayParts[0] : "";
                        String[] recordedDaysArray = recordedDaysStr.isEmpty() ? new String[0] : recordedDaysStr.split(DAYS_DET);

                        for (String d : recordedDaysArray) {
                            try {
                                loginDays.add(Integer.parseInt(d));
                            } catch (NumberFormatException ignored) {}
                        }

                        try {
                            recordedWeek = Integer.parseInt(parts[4]);
                        } catch (NumberFormatException ignored) {}

                        try {
                            totalLoggedInDays = Integer.parseInt(parts[5]);
                        } catch (NumberFormatException ignored) {}

                        matchedLine = line;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("讀檔時錯誤!");
            return;
        }

        if (!userFound) {
            showError("找不到使用者名稱!");
            return;
        }

        if (!inputP.equals(storedP)) {
            showError("密碼錯誤!");
            return;
        }

        // Password is correct: update login data
        LocalDate today = LocalDate.now();
        int currentDayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());

        if (recordedWeek != currentWeek) {
            loginDays.clear();
            loginDays.add(currentDayOfWeek);
            if (currentDayOfWeek == 7 || weekLogin > 7) {
            	weekLogin = 1;
            }else {
            	weekLogin++;
            }
            totalLoggedInDays++;  // First login of a new week, so it's a new day
        } else if (!loginDays.contains(currentDayOfWeek)) {
            loginDays.add(currentDayOfWeek);
            weekLogin++;
            totalLoggedInDays++;  // First login of the day in current week
        }

        Collections.sort(loginDays);
        StringBuilder sb = new StringBuilder();
        for (int day : loginDays) {
            sb.append(day).append(DAYS_DET);
        }
        sb.append(FIELD_DET); // Ending "XXXX"

        String updatedLine = inputU + FIELD_DET + storedP + FIELD_DET + weekLogin + FIELD_DET +
                sb + FIELD_DET + currentWeek + FIELD_DET + totalLoggedInDays;

        // Rebuild file with updated line
        List<String> updatedLines = new ArrayList<>();
        for (String line : allLines) {
            if (line.equals(matchedLine)) {
                updatedLines.add(updatedLine + "\n");
            } else {
                updatedLines.add(line + "\n");
            }
        }

        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (String updated : updatedLines) {
                writer.write(updated + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("更新登入資料時錯誤!");
            return;
        }

        Integer[] loginDaysArray = loginDays.toArray(new Integer[0]);
        loginSuccess(inputU, stage, loginDaysArray, totalLoggedInDays);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //enrolls user
    /*
     * FORMAT: name XXXX password XXXX login days in a week XXXX current week XXXX total logged in days
     */
    private void handleEnroll() {
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();

        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("Username and Password cannot be empty");
            return;
        }

        // Check for duplicate user names
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(FIELD_DET);
                if (parts.length >= 1 && parts[0].equals(inputU)) {
                    showError("Username already exists!");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error checking existing users");
            return;
        }

        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int totalLoggedInDays = 1; // First login upon enrollment
        int weekLogin = 1;

        String loginDays = currentDay + DAYS_DET; // e.g., "1X"

        try (FileWriter writer = new FileWriter(fileName, true)) {  // true = append
            writer.write(inputU + FIELD_DET + inputP + FIELD_DET + weekLogin + FIELD_DET +
                         loginDays + WEEK_DET + FIELD_DET + currentWeek + FIELD_DET + totalLoggedInDays + "\n");
            showSuccess("Enrollment successful!");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error enrolling user");
        }
    }

    public int[] getWeekLogin(String username) {
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(FIELD_DET);

                if (parts.length >= 4 && parts[0].equals(username)) {
                    String loginDaysRaw = parts[3]; // example: 0X1X2X3X X

                    // Remove the trailing "XX" and split by "X"
                    String[] dayParts = loginDaysRaw.split(FIELD_DET)[0].split(DAYS_DET);

                    List<Integer> dayList = new ArrayList<>();
                    for (String part : dayParts) {
                        if (!part.isEmpty()) {
                            try {
                                int day = Integer.parseInt(part);
                                if (!dayList.contains(day)) {
                                    dayList.add(day);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    // Convert list to array
                    int[] result = new int[dayList.size()];
                    for (int i = 0; i < dayList.size(); i++) {
                        result[i] = dayList.get(i);
                    }
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If not found or error, return empty array
        return new int[0];
    }

    private void handleClearFile() {
        File file = new File(fileName);
        if (file.exists() && file.delete()) {
            showSuccess("User data file cleared");
        } else {
            showError("Error clearing user data file");
        }
        System.exit(0);
    }
}
