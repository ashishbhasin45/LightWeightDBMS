package Assignment1.Storage;

import java.util.ArrayList;
import java.util.List;

public class Rows {
    private String tableName;
    private List<String> rows;

    public Rows(String tableName, String row) {
        this.tableName = tableName;
        this.rows = new ArrayList<>();
        this.rows.add(row);
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getRows() {
        return List.copyOf(this.rows);
    }

    public void addRow(String row){
        this.rows.add(row);
    }
}
