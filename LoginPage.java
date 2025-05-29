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

	private final File dataFolder = new File("data");
	private final File userDataFile = new File(dataFolder, "UserNames.txt");

	private final String FIELD_DET = "XXXX";
	private final int currentWeek = getCurrentWeek();
	private final int currentDay = getCurrentDay();
    private TextField tfUserName;
    private PasswordField tfPassword;
    private Button btnLogin, btnEnroll, btnClearFile;
    private String fileName = "UserNames.txt";

    public void createLoginPage(Stage stage) {
    	
    	fileCheck(userDataFile);

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

        btnLogin.setOnAction(e -> handleLogin(stage));
        btnEnroll.setOnAction(e -> handleEnroll());
        btnClearFile.setOnAction(e -> handleClearFile());
        
        button.getChildren().addAll(btnLogin, btnEnroll, btnClearFile);
        layout.getChildren().addAll(tfUserName, tfPassword, button);

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("記帳軟體登入");
        stage.setScene(scene);
        stage.show();
    }
    
    //Checks if file exists, if not, create a new file.
    private void fileCheck(File userFile) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();  // Create the folder if missing
            System.out.println("Created data directory.");
        }

        if (!userFile.exists()) {
            try {
                if (userFile.createNewFile()) {
                	showSuccess("File creation successful!");
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
        String storedP = null; // stored password
        StringBuilder weekLogin = new StringBuilder(); // login days in a week
        String tempWeekLogin = " "; // temp
        int recordedWeek = 0; // record week
        int recordedDay = 0; // //day
        int hasLoggedIn = 1; // same day login indicator
        String matchedLine = null;
        List<String> allLines = new ArrayList<>();

        try (Scanner sc = new Scanner(new File(userDataFile.getPath()))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                allLines.add(line);
                String[] parts = line.split(FIELD_DET);

                if (parts.length >= 7) {
                    String storedU = parts[0];
                    if (storedU.equals(inputU)) {
                        userFound = true;
                        storedP = parts[1];
                        tempWeekLogin = parts[2];
                        hasLoggedIn = Integer.parseInt(parts[3]);
                        totalLoggedInDays = Integer.parseInt(parts[4]);
                        recordedWeek = Integer.parseInt(parts[5]);
                        recordedDay = Integer.parseInt(parts[6]);
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

        // Get current date info
        LocalDate today = LocalDate.now();
        int currentDayOfWeek = today.getDayOfWeek().getValue(); // 1 = Monday
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());

        // Reset hasLoggedIn if it's a new day
        if (recordedDay != currentDayOfWeek) {
            hasLoggedIn = 0;
        }

        // Update login state
     // Update login info
        if (recordedDay != currentDayOfWeek) {
            hasLoggedIn = 0;
        }

        if (recordedWeek != currentWeek || hasLoggedIn == 0) {
            totalLoggedInDays++;
            hasLoggedIn = 1;
        }

        String weekLoginStr = buildNormalizedWeekLogin(tempWeekLogin, recordedWeek, recordedDay,
                                                        currentWeek, currentDayOfWeek, hasLoggedIn);

        // Prepare updated line
        String updatedLine = inputU + FIELD_DET + storedP + FIELD_DET + weekLoginStr + FIELD_DET +
                hasLoggedIn + FIELD_DET + totalLoggedInDays + FIELD_DET +
                currentWeek + FIELD_DET + currentDayOfWeek + "\n";
        
        // Rebuild file content
        List<String> updatedLines = new ArrayList<>();
        for (String line : allLines) {
            if (line.equals(matchedLine)) {
                updatedLines.add(updatedLine);
            } else {
                updatedLines.add(line);
            }
        }

        try (FileWriter writer = new FileWriter(userDataFile, false)) {
            for (String updated : updatedLines) {
                writer.write(updated + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("更新登入資料時錯誤!");
            return;
        }
        
        String[] updatedWeekLoginS = weekLoginStr.split("\\.");
        Integer[] updatedWeekLoginI = new Integer[updatedWeekLoginS.length];

        for (int i = 0; i < 7; i++) {
            if(updatedWeekLoginS[i].equals("1")) {
            	updatedWeekLoginI[i] = i + 1;
            }
        }

        loginSuccess(inputU, stage, updatedWeekLoginI, totalLoggedInDays);
    }


    //Passes the data after success login
    private void loginSuccess(String name, Stage stage, Integer[] loginDaysArray, int totalLoggedInDays) {
        Project_Accounting app = new Project_Accounting();
        stage.setTitle("記帳軟體");
        app.start(stage);
        app.setupMainMenu(name, stage, loginDaysArray, totalLoggedInDays);

    }

    //Identical to normal login only with the name and password set to mine
    
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
     * FORMAT: name password weeklogin hasloggedin totalloggedin currentweek currentday
     */
    private void handleEnroll() {
    	
        String inputU = tfUserName.getText().trim();
        String inputP = tfPassword.getText().trim();
        
        if (inputU.isEmpty() || inputP.isEmpty()) {
            showError("Username and Password cannot be empty");
            return;
        }

        // Check for duplicate user names
        try (Scanner sc = new Scanner(new File(userDataFile.getPath()))) {
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

        int totalLoggedInDays = 0;
        int weekLogin = 0;
        int hasLoggedIn = 0;

        try (FileWriter writer = new FileWriter(userDataFile.getPath(), true)) {  // true = append
            writer.write(inputU + FIELD_DET + inputP + FIELD_DET + weekLogin + FIELD_DET +
                    hasLoggedIn + FIELD_DET + totalLoggedInDays + FIELD_DET + currentWeek + FIELD_DET + currentDay +"\n");
            /*
             * FORMAT: name password weeklogin hasloggedin totalloggedin currentweek currentday
             */
            showSuccess("Enrollment successful!");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error enrolling user");
        }
    }
    
    //Don't touch
    private String buildNormalizedWeekLogin(String previousLogin, int recordedWeek, int recordedDay,
            int currentWeek, int currentDayOfWeek, int hasLoggedIn) {
    			int[] days = new int[7]; // default to 0s

    			// Fill previous login data if it's same week
    			if (recordedWeek == currentWeek) {
    				String[] split = previousLogin.split("\\.");
    				for (int i = 0; i < Math.min(split.length, 7); i++) {
    					try {
    						days[i] = Integer.parseInt(split[i]);
    					} catch (NumberFormatException e) {
    						days[i] = 0;
    					}
    				}

    				// If not logged in yet today, mark it
    				if (hasLoggedIn == 0) {
    					days[currentDayOfWeek - 1] = 1;
    				}
    			} else {
    				// New week, only mark today
    				days[currentDayOfWeek - 1] = 1;
    			}

    			// Build string
    			StringBuilder sb = new StringBuilder();
    			for (int i = 0; i < 7; i++) {
    				sb.append(days[i]).append(".");
    			}
    			
    			return sb.toString();
    }

    private int getCurrentWeek() {
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }
    
    private int getCurrentDay() {
    	Calendar calendar = Calendar.getInstance();
    	int day = calendar.get(Calendar.DAY_OF_WEEK); 
		return day;
    }
    
    private void handleClearFile() {
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            File[] files = dataFolder.listFiles();
            boolean allDeleted = true;

            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                        allDeleted = false;
                    }
                }
            }

            if (allDeleted) {
                showSuccess("All data files cleared.");
            } else {
                showError("Some files could not be deleted.");
            }
        } else {
            showError("Data folder does not exist.");
        }
        System.exit(0);  // Close application
    }

}
