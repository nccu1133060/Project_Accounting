import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class RecordView {
    private ListView<String> recordList = new ListView<>();
    private PieChart pieChart = new PieChart();

    public Parent getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "日常確幸", "服飾", "欠款", "通勤", "其他");
        categoryBox.getSelectionModel().selectFirst();

        TextField itemField = new TextField();
        itemField.setPromptText("項目名稱");

        TextField amountField = new TextField();
        amountField.setPromptText("金額");

        Button submitBtn = new Button("新增紀錄");

        root.getChildren().addAll(datePicker, categoryBox, itemField, amountField, submitBtn, recordList, pieChart);

        // 按下新增紀錄時動作
        submitBtn.setOnAction(e -> {
            try {
                String date = datePicker.getValue().toString();
                String category = categoryBox.getValue();
                String name = itemField.getText();
                double amount = Double.parseDouble(amountField.getText());

                Record newRecord = new Record(date, category, name, amount);
                Project_Accounting.records.add(newRecord);
                recordList.getItems().add(newRecord.toString());

                updatePieChartWithSummary(pieChart, Project_Accounting.records);

                // --- 新增：支出預警判斷 (超過預算80%) ---
                double totalSpentInCategory = Project_Accounting.records.stream()
                    .filter(r -> r.category.equals(category))
                    .mapToDouble(r -> r.amount)
                    .sum();

                double budgetForCategory = TaskView.getBudgetForCategory(category);

                if (budgetForCategory > 0 && totalSpentInCategory > budgetForCategory * 0.8) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("預算警告");
                    alert.setHeaderText("您的支出已超過預算的80%！");
                    alert.setContentText("類別「" + category + "」預算為 " + budgetForCategory + " 元，" +
                            "目前支出已達 " + String.format("%.2f", totalSpentInCategory) + " 元。");
                    alert.showAndWait();
                }

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "輸入資料格式錯誤或不完整！");
                alert.showAndWait();
            }
        });

        return root;
    }

    private void updatePieChartWithSummary(PieChart pieChart, List<Record> records) {
        Map<String, Double> categorySum = new HashMap<>();
        for (Record r : records) {
            categorySum.put(r.category, categorySum.getOrDefault(r.category, 0.0) + r.amount);
        }

        pieChart.getData().clear();
        for (Map.Entry<String, Double> entry : categorySum.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }
}
