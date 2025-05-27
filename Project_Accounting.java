import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Project_Accounting extends Application {
    private BorderPane mainLayout;
    private VBox mainMenu;
    private PieChart mainPieChart;
    public static ArrayList<Record> records = new ArrayList<>();
    public static PieChart getSharedPieChart() {
        return new PieChart();
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("記帳軟體");
        mainLayout = new BorderPane();
        setupMainChart();

        mainLayout.setLeft(mainMenu);
        mainLayout.setCenter(mainPieChart);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void setupMainMenu(String name, Stage stage, Integer[] loginDaysArray, int totalLoggedInDays) {
        mainMenu = new VBox(10);
        Set<Integer> LDA = new HashSet<>(Arrays.asList(loginDaysArray));
        Button btnRecord = new Button("記帳");
        Button btnCalendar = new Button("記帳日曆");
        Button btnTask = new Button("任務");

        btnRecord.setOnAction(e -> mainLayout.setCenter(new RecordView().getView(mainPieChart)));
        btnCalendar.setOnAction(e -> mainLayout.setCenter(new CalendarView().getView(LDA, totalLoggedInDays)));
        btnTask.setOnAction(e -> mainLayout.setCenter(new TaskView().getView(name)));

        mainMenu.getChildren().addAll(btnRecord, btnCalendar, btnTask);
    }

    private void setupMainChart() {
        mainPieChart = new PieChart();
        mainPieChart.setTitle("支出分類圖表");
        ChartUtil.updatePieChart(mainPieChart, records);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
