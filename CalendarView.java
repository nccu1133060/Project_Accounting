import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class CalendarView {

    private static final int CIRCLE_RADIUS = 30;
    private LocalDate ld = LocalDate.now();
    public StackPane getView(Set<Integer> loggedInDays, int totalLoggedInDays) {
    	
        StackPane root = new StackPane();
        VBox content = new VBox(15); // 15px space between row and label
        content.setAlignment(Pos.CENTER);

        HBox circleRow = new HBox(15);
        circleRow.setAlignment(Pos.CENTER);

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        int currentDayIndex = currentDay.getValue(); // Monday = 1

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (int i = 1; i <= 7; i++) {
            Circle mainCircle = new Circle(CIRCLE_RADIUS);
            Text label = new Text(dayNames[i - 1]);

            if (i == currentDayIndex) {
                mainCircle.setFill(Color.RED);
            } else {
                mainCircle.setFill(Color.GREEN);
            }

            StackPane circleWithRing = new StackPane();

            Circle goldenRing = null;
            if (loggedInDays.contains(i)) {
                goldenRing = new Circle(CIRCLE_RADIUS + 5);
                goldenRing.setFill(Color.TRANSPARENT);
                goldenRing.setStroke(Color.GOLD);
                goldenRing.setStrokeWidth(3);
                circleWithRing.getChildren().add(goldenRing); // Add first
            }

            circleWithRing.getChildren().add(mainCircle); // Add after so it's on top

            VBox dayBox = new VBox(5, circleWithRing, label);
            dayBox.setAlignment(Pos.CENTER);
            circleRow.getChildren().add(dayBox);
        }


        // Add login count label
        Text loginCount = new Text("Total login days: " + totalLoggedInDays);
        loginCount.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        Text date = new Text("Today is: " + ld + " (yyyy-mm-dd)");
        date.setStyle("-fx-font-size: 28px;");
        content.getChildren().addAll(date, circleRow, loginCount);
        root.getChildren().add(content);

        return root;
    }
}
