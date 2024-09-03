package Assignment1.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tables {
    private Map<String, Rows> tableDetails;
    private Map<String, List<ColumnDetail>> columnDetails;

    public Tables() {
        this.tableDetails = new HashMap<>();
        this.columnDetails = new HashMap<>();
    }

    public void addRowToTable(String tableName, String row){
        if(this.tableDetails.containsKey(tableName)){
            this.tableDetails.get(tableName).addRow(row);
        }
        else{
            this.tableDetails.put(tableName, new Rows(tableName, row));
        }
    }

    public void removeTableDetailFromSet(String tableName){
        if(this.tableDetails.containsKey(tableName)){
            this.tableDetails.remove(tableName);
        }
    }

    public boolean addColumnDetailsForTable(String tableName, String columnDetail){
        if(!this.columnDetails.containsKey(tableName)){
            String[] columns = columnDetail.split("\\|");
            List<ColumnDetail> columnDefinitions = new ArrayList<>(columns.length);
            for(String column: columns){
                var columnDef = new ColumnDetail(column.split(",")[0].trim(),
                        column.split(",")[0].trim());
                columnDefinitions.add(columnDef);
            }
            this.columnDetails.put(tableName, columnDefinitions);
            return true;
        }
        else{
            // column definition already exists for the table
            return false;
        }
    }

    public Map<String, Rows> getTableDetails(){
        return this.tableDetails;
    }

    public Map<String, List<ColumnDetail>> getColumnDetails() {
        return this.columnDetails;
    }

    public Rows getTable(String tableName){
        return this.tableDetails.get(tableName);
    }
}
