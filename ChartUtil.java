import javafx.scene.chart.PieChart;
import java.util.*;

	public class ChartUtil {
	    public static Map<String, Double> updatePieChart(PieChart chart, List<Record> records) {
	        Map<String, Double> categoryTotals = new HashMap<>();

	        for (Record r : records) {
	            categoryTotals.put(r.category, categoryTotals.getOrDefault(r.category, 0.0) + r.amount);
	        }

	        chart.getData().clear();

	        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
	        if (total == 0) total = 1;

	        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
	            double percentage = (entry.getValue() / total) * 100;
	            chart.getData().add(
	                new PieChart.Data(entry.getKey() + " " + String.format("%.1f%%", percentage), entry.getValue())
	            );
	        }

	        return categoryTotals;
	    }
}

