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
    private static final int RING_WIDTH = 3;
    private static final double SPACING = 15;
    private static final Color CURRENT_DAY_COLOR = Color.RED;
    private static final Color DEFAULT_DAY_COLOR = Color.GREEN;
    private static final Color RING_COLOR = Color.GOLD;
    private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public StackPane getView(Set<Integer> loggedInDays, int totalLoggedInDays) {
        LocalDate today = LocalDate.now();
        int currentDayIndex = today.getDayOfWeek().getValue(); // Monday = 1

        // Ensure today is marked as a logged-in day
        loggedInDays.add(currentDayIndex);

        VBox content = new VBox(SPACING);
        content.setAlignment(Pos.CENTER);

        HBox circleRow = new HBox(SPACING);
        circleRow.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 7; i++) {
            circleRow.getChildren().add(createDayCircle(i, currentDayIndex, loggedInDays));
        }

        Text dateText = createStyledText("Today is: " + today + " (yyyy-mm-dd)", 28, false);
        Text loginCountText = createStyledText("Total login days: " + totalLoggedInDays, 28, true);

        content.getChildren().addAll(dateText, circleRow, loginCountText);

        return new StackPane(content);
    }


    private VBox createDayCircle(int dayIndex, int currentDayIndex, Set<Integer> loggedInDays) {
        Circle mainCircle = new Circle(CIRCLE_RADIUS);
        mainCircle.setFill(dayIndex == currentDayIndex ? CURRENT_DAY_COLOR : DEFAULT_DAY_COLOR);

        StackPane circleStack = new StackPane();
        circleStack.getChildren().add(mainCircle); // Add the base circle first

        if (loggedInDays.contains(dayIndex)) {
            Circle ring = new Circle(CIRCLE_RADIUS + RING_WIDTH);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(RING_COLOR);
            ring.setStrokeWidth(RING_WIDTH);
            circleStack.getChildren().add(ring); // Add the ring on top of the base circle
        }

        Text label = new Text(DAY_NAMES[dayIndex - 1]);
        VBox dayBox = new VBox(5, circleStack, label);
        dayBox.setAlignment(Pos.CENTER);
        return dayBox;
    }


    private Text createStyledText(String content, int fontSize, boolean bold) {
        Text text = new Text(content);
        text.setStyle(String.format("-fx-font-size: %dpx; %s", fontSize, bold ? "-fx-font-weight: bold;" : ""));
        return text;
    }
}
