import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class RecordView {
    private ListView<String> recordList = new ListView<>();
    private VBox summaryBox = new VBox(5); 

    public BorderPane getView(PieChart pieChart) {
        DatePicker datePicker = new DatePicker();
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("食物", "日常確幸", "服飾", "欠款", "通勤", "其他");

        TextField itemField = new TextField();
        itemField.setPromptText("請輸入項目名稱");

        TextField amountField = new TextField();
        amountField.setPromptText("請輸入金額");

        Button submitBtn = new Button("送出");
        
        Button deleteCategoryBtn = new Button("刪除類別紀錄");
        deleteCategoryBtn.setOnAction(e -> {
            String selectedCategory = categoryBox.getValue();
            if (selectedCategory != null) {
                Project_Accounting.records.removeIf(r -> r.category.equals(selectedCategory));
                recordList.getItems().removeIf(s -> s.contains("| " + selectedCategory + " |"));
                updatePieChartWithSummary(pieChart, Project_Accounting.records);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "請選擇要刪除的類別！");
                alert.showAndWait();
            }
        });

        HBox inputRow = new HBox(10,
                new Label("日期:"), datePicker,
                new Label("類別:"), categoryBox);
        
        VBox inputV = new VBox(10,
                new Label("項目:"), itemField,
                new Label("金額:"), amountField, submitBtn, deleteCategoryBtn);
        
        inputRow.setPadding(new Insets(10));

        submitBtn.setOnAction(e -> {
            try {
                String date = datePicker.getValue().toString();
                String category = categoryBox.getValue();
                String name = itemField.getText();
                double amount = Double.parseDouble(amountField.getText());

                Record newRecord = new Record(date, category, name, amount);
                Project_Accounting.records.add(newRecord);
                recordList.getItems().add(newRecord.toString());

                // 新增功能：支出預警（當該類別支出超過所有支出80%時）
                double totalAmount = Project_Accounting.records.stream()
                        .mapToDouble(r -> r.amount)
                        .sum();
                double categoryAmount = Project_Accounting.records.stream()
                        .filter(r -> r.category.equals(category))
                        .mapToDouble(r -> r.amount)
                        .sum();

                double warningThreshold = 0.8; // 50%
                if (totalAmount > 0 && (categoryAmount / totalAmount) > warningThreshold) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("支出預警");
                    alert.setHeaderText(null);
                    alert.setContentText("警告！類別「" + category + "」的支出已超過總支出的 80%！");
                    alert.showAndWait();
                }

                updatePieChartWithSummary(pieChart, Project_Accounting.records);
            } catch (Exception ex) {
                Parent parent = inputRow.getParent();
                if (parent instanceof Pane pane) {
                    pane.getChildren().remove(inputRow);
                }
            }
        });
        
        BorderPane layout = new BorderPane();
        layout.setTop(inputRow);
        layout.setCenter(recordList);

        VBox rightPanel = new VBox(10, inputV, pieChart, summaryBox);
        rightPanel.setPadding(new Insets(10));
        layout.setRight(rightPanel);

        return layout;
    }

    private void updatePieChartWithSummary(PieChart chart, List<Record> records) {
        try {
            Map<String, Double> categoryTotals = new HashMap<>();
            for (Record r : records) {
                categoryTotals.put(r.category, categoryTotals.getOrDefault(r.category, 0.0) + r.amount);
            }

            chart.getData().clear();
            double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) total = 1; 

            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                double percentage = (entry.getValue() / total) * 100;
                chart.getData().add(new PieChart.Data(entry.getKey() + " " + String.format("%.1f%%", percentage), entry.getValue()));                
            }

            summaryBox.getChildren().clear();
            summaryBox.getChildren().add(new Label("各類別總額：" + total));
            for (String category : Arrays.asList("吃的", "日常確幸", "服飾", "欠款", "通勤", "其他")) {
                double sum = categoryTotals.getOrDefault(category, 0.0);
                summaryBox.getChildren().add(new Label(category + ": " + sum + " 元"));
            }

        } catch (Exception e) {
            chart.setTitle("資料錯誤：更新失敗但仍保留之前的資料");
        }
    }
}
