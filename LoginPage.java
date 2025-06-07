import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class LoginPage {

    // 檔案系統設定
    private final File dataFolder = new File("data");
    private final File userDataFile = new File(dataFolder, "UserNames.txt");
    private final String FIELD_DELIMITER = "XXXX";
    
    // 資料庫設定
    private final String DB_URL = " http://140.119.19.73/phpmyadmin";
    private final String DB_USER = "TG12";
    private final String DB_PASSWORD = "nkH3Iq";
    private final String TABLE_NAME = "users";
    private Connection connection;
    
    // 時間相關
    private final int currentWeek = getCurrentWeek();
    private final int currentDay = getCurrentDay();
    
    // UI元件
    private TextField tfUserName;
    private PasswordField tfPassword;
    private Button btnLogin, btnEnroll, btnClearFile;

    public void createLoginPage(Stage stage) {
        initializeDatabase(); // 初始化資料庫連接
        syncFromDatabase();  // 從資料庫同步資料

        // UI初始化
        VBox layout = new VBox(10);
        tfUserName = new TextField();
        tfUserName.setPromptText("使用者名稱");
        
        tfPassword = new PasswordField();
        tfPassword.setPromptText("輸入密碼");
        
        btnLogin = new Button("登入");
        btnEnroll = new Button("註冊");
        btnClearFile = new Button("清除檔案並關閉程式（按下前請三思）");
        btnClearFile.setStyle("-fx-background-color: #8B0000; -fx-text-fill: #FFFFFF;");
        
        // 事件處理
        btnLogin.setOnAction(e -> handleLogin(stage));
        btnEnroll.setOnAction(e -> handleEnroll());
        btnClearFile.setOnAction(e -> handleClearFile());
        
        layout.getChildren().addAll(tfUserName, tfPassword, btnLogin, btnEnroll, btnClearFile);
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("記帳軟體登入");
        stage.show();
    }

    //資料庫相關方法
    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTableIfNotExists(); // 確保資料表存在
        } catch (Exception e) {
            System.err.println("資料庫連接失敗，將使用本地儲存: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                     "username VARCHAR(50) PRIMARY KEY, " +
                     "password VARCHAR(50) NOT NULL, " +
                     "week_login VARCHAR(20) NOT NULL, " +
                     "has_logged_in INT NOT NULL, " +
                     "total_logged_in_days INT NOT NULL, " +
                     "current_week INT NOT NULL, " +
                     "current_day INT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void syncFromDatabase() {
        if (!isDatabaseConnected()) return;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE_NAME)) {
            
            // 清空本地檔案
            try (PrintWriter writer = new PrintWriter(userDataFile)) {
                while (rs.next()) {
                    writer.printf("%s%s%s%s%s%s%d%s%d%s%d%s%d%n",
                        rs.getString("username"), FIELD_DELIMITER,
                        rs.getString("password"), FIELD_DELIMITER,
                        rs.getString("week_login"), FIELD_DELIMITER,
                        rs.getInt("has_logged_in"), FIELD_DELIMITER,
                        rs.getInt("total_logged_in_days"), FIELD_DELIMITER,
                        rs.getInt("current_week"), FIELD_DELIMITER,
                        rs.getInt("current_day"));
                }
            }
        } catch (SQLException | IOException e) {
            showError("從資料庫同步失敗: " + e.getMessage());
        }
    }

    private void syncToDatabase() {
        if (!isDatabaseConnected()) return;
        
        try {
            // 清空資料庫表
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("TRUNCATE TABLE " + TABLE_NAME);
            }
            
            // 從本地檔案讀取並寫入資料庫
            try (Scanner scanner = new Scanner(userDataFile);
                 PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(FIELD_DELIMITER);
                    if (parts.length == 7) {
                        pstmt.setString(1, parts[0]);
                        pstmt.setString(2, parts[1]);
                        pstmt.setString(3, parts[2]);
                        pstmt.setInt(4, Integer.parseInt(parts[3]));
                        pstmt.setInt(5, Integer.parseInt(parts[4]));
                        pstmt.setInt(6, Integer.parseInt(parts[5]));
                        pstmt.setInt(7, Integer.parseInt(parts[6]));
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException | IOException e) {
            showError("同步到資料庫失敗: " + e.getMessage());
        }
    }

    private boolean isDatabaseConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

   
    private void handleLogin(Stage stage) {
        String username = tfUserName.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("使用者名稱或密碼不得為空！");
            return;
        }

        // 從本地檔案驗證
        boolean isValid = false;
        try (Scanner scanner = new Scanner(userDataFile)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(FIELD_DELIMITER);
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    isValid = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            showError("找不到使用者資料檔案！");
            return;
        }

        if (isValid) {
            syncToDatabase(); // 登入成功時同步到資料庫
            showSuccess("登入成功！");
            // 這裡可以繼續原有登入成功後的處理
        } else {
            showError("使用者名稱或密碼錯誤！");
        }
    }

    private void handleEnroll() {
        String username = tfUserName.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("使用者名稱或密碼不得為空！");
            return;
        }

        // 檢查是否已存在（本地+資料庫）
        boolean exists = false;
        try (Scanner scanner = new Scanner(userDataFile)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(FIELD_DELIMITER);
                if (parts.length >= 1 && parts[0].equals(username)) {
                    exists = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            // 檔案不存在，繼續註冊
        }

        if (!exists && isDatabaseConnected()) {
            // 檢查資料庫中是否已存在
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT username FROM " + TABLE_NAME + " WHERE username = ?")) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                    }
                }
            } catch (SQLException e) {
                showError("檢查資料庫時發生錯誤: " + e.getMessage());
                return;
            }
        }

        if (exists) {
            showError("該使用者名稱已存在！");
            return;
        }

        // 寫入本地檔案
        try (FileWriter writer = new FileWriter(userDataFile, true)) {
            writer.write(username + FIELD_DELIMITER + password + FIELD_DELIMITER + 
                        "0.0.0.0.0.0.0" + FIELD_DELIMITER + "0" + FIELD_DELIMITER + 
                        "0" + FIELD_DELIMITER + currentWeek + FIELD_DELIMITER + 
                        currentDay + "\n");
            
            // 如果資料庫連接正常，同時寫入資料庫
            if (isDatabaseConnected()) {
                try (PreparedStatement pstmt = connection.prepareStatement(
                        "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.setString(3, "0.0.0.0.0.0.0");
                    pstmt.setInt(4, 0);
                    pstmt.setInt(5, 0);
                    pstmt.setInt(6, currentWeek);
                    pstmt.setInt(7, currentDay);
                    pstmt.executeUpdate();
                }
            }
            
            showSuccess("註冊成功！");
        } catch (IOException | SQLException e) {
            showError("註冊時發生錯誤: " + e.getMessage());
        }
    }

    private void handleClearFile() {
        // 清除本地檔案
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
                showSuccess("所有本地檔案已清除！");
            } else {
                showError("部分檔案無法刪除！");
            }
        } else {
            showError("找不到資料目錄！");
        }
        
        // 如果資料庫連接正常，清除資料庫
        if (isDatabaseConnected()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("TRUNCATE TABLE " + TABLE_NAME);
                showSuccess("資料庫資料也已清除！");
            } catch (SQLException e) {
                showError("清除資料庫時發生錯誤: " + e.getMessage());
            }
        }
        
        System.exit(0);
    }

   
    private int getCurrentWeek() {
        return LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    private int getCurrentDay() {
        return LocalDate.now().getDayOfWeek().getValue(); // 1=Monday,...,7=Sunday
    }

    private void showError(String message) {
        new Alert(AlertType.ERROR, message).showAndWait();
    }

    private void showSuccess(String message) {
        new Alert(AlertType.INFORMATION, message).showAndWait();
    }

    public void close() {
        if (isDatabaseConnected()) {
            try {
                syncToDatabase(); // 關閉前同步資料
                connection.close();
            } catch (SQLException e) {
                System.err.println("關閉資料庫連接時出錯: " + e.getMessage());
            }
        }
    }
}
