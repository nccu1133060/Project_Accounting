public class Record {
    String date, category,name;
    double amount;

    public Record(String date, String category, String name,double amount) {
        this.date = date;
        this.category = category;
        this.name = name;
        this.amount = amount;
    }

    public String toString() {
        return date + " | " + category + " | " + name + " | $" + amount;
    }
}