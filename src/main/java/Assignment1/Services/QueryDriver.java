package Assignment1.Services;

import Assignment1.Helpers.QueryHelper;
import Assignment1.Storage.Rows;
import Assignment1.Storage.Tables;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryDriver implements IQueryDriver {
    private final ITransactionManager _transactionManager;
    private final ILogManager _logger;
    private final IStorageService _StorageService;
    public QueryDriver(ITransactionManager _transactionManager, ILogManager logger, IStorageService storageService) {
        this._transactionManager = _transactionManager;
        this._logger = logger;
        this._StorageService = storageService;
    }

    /**
     * Processes the input query
     * @param query string query received from user
     * @param userId user id of the logged-in user
     * @return a boolean flag denoting if the query was processed successfully or not
     */
    @Override
    public boolean ProcessQuery(String query, String userId) {
        var queryOutput = this.parseAndValidateQuery(query);
        if (queryOutput != null) {
            // if queryResult not null, that means query is valid and processed, add entry to logs
          return  _logger.WriteLog(userId, query);
        }

        return  false;
    }

    /**
     * Validates and parses the query
     * @param query input query
     * @return in memory table data after query execution
     */
    private Tables parseAndValidateQuery(String query){
        if(query == null || query.trim().isEmpty()){
            return null;
        }

        // Begin, commit and rollback Transaction queries
        switch (query.trim().toLowerCase()){
            case QueryHelper.beginTransaction -> {
                if (_transactionManager.getTransaction()) {
                    System.out.println("Cannot Begin a transaction before ending previous transaction");
                    return null;
                }

                _transactionManager.beginTransaction();
                return _transactionManager.getTables();
            }
            case QueryHelper.commitTransaction -> {
                if (!_transactionManager.getTransaction()) {
                    System.out.println("Cannot commit before begin transaction");
                    return null;
                }

                if (!commitTransaction()) {
                    return null;
                }

                return _transactionManager.getTables();
            }
            case  QueryHelper.rollbackTransaction -> {
                if (!_transactionManager.getTransaction()) {
                    System.out.println("Cannot rollback before begin transaction");
                    return null;
                }

                this._transactionManager.rollBackTransaction();
                return _transactionManager.getTables();
            }
        }

        // Crud operation queries
        switch (query.split(" ")[0].toLowerCase()){
            case QueryHelper.insertQueryMatcher -> {
               return InsertQueryHandler(query);
            }
            case QueryHelper.createQueryMatcher -> {
                return CreateQueryHandler(query);
            }
            case QueryHelper.selectQueryMatcher -> {
                return SelectQueryHandler(query);
            }
            default -> {return null;}
        }
    }

    /**
     * handles create query validation and parsing
     * @param query query
     * @return in memory table after query execution
     */
    private Tables CreateQueryHandler(String query){
        try {
            if (query != null) {
                // not allowing creation of table within a transaction
                if(_transactionManager.getTransaction()){
                    System.out.println("Table cannot be created within a transaction");
                    return null;
                }
                String[] queryArray = query.split("\\s+");
                if (!queryArray[1].equalsIgnoreCase("table"))
                    return null;

                // validate and get table name
                String tableName = queryArray[2];
                if (!tableName.matches(QueryHelper.tableRegex)) {
                    System.out.println("Provide a valid table name");
                    return null;
                }

                boolean tablePresent = _StorageService.CheckDataStoreExists(tableName);
                if (tablePresent) {
                    System.out.println("Table already present");
                    return null;
                }

                // get table validation and validate
                Pattern pattern = Pattern.compile(QueryHelper.tableDefinitionGroupRegex);
                Matcher matcher = pattern.matcher(query);
                if (!matcher.find() && matcher.groupCount() != 1) {
                    return null;
                }

                String[] tableColumns = matcher.group(1).split(",");
                StringBuilder tableDetails = new StringBuilder();
                for (String columnDetails : tableColumns) {
                    String[] columnNameAndDef = columnDetails.trim().split("\\s");
                    String columnName = columnNameAndDef[0];
                    String columnDataType = columnNameAndDef[1];
                    // if column names are not valid
                    if (!columnName.matches(QueryHelper.tableRegex)) {
                        System.out.println("Check syntax");
                        return null;
                    }
                    // validate for column datatype and get corresponding language specific dataType
                    String dataType = QueryHelper.validateAndReturnType(columnDataType.toLowerCase());
                    if (dataType == null) {
                        System.out.println("Provide valid dataTypes");
                        return null;
                    }

                    tableDetails.append(columnName);
                    tableDetails.append(",");
                    tableDetails.append(dataType);
                    tableDetails.append(QueryHelper.columnSplitter);
                }

                // delete last delimiter from the line
                tableDetails.deleteCharAt(tableDetails.lastIndexOf(QueryHelper.columnSplitter));

                boolean fileCreated = _StorageService.CreateDataStoreWithContent(tableName, tableDetails.toString());
                if (!fileCreated) {
                    return null;
                }

                return this._transactionManager.getTables();
            }

            return null;
        }catch (Exception ex){
           return null;
        }
    }

    /**
     * Insert query handler
     * @param query input query
     * @return in memory table after query execution
     */
    private Tables InsertQueryHandler(String query){
        if(query != null){
            var tables = InsertIntoInMemoryStore(query);
            if(tables == null){
                return null;
            }
            else if(!this._transactionManager.getTransaction()){
                boolean rowUpdated = this.writeTableDataToFiles(tables);
                if(!rowUpdated){
                    return null;
                }
            }
            return _transactionManager.getTables();
        }

        return null;
    }

    /**
     * Inserts data into in memory table store
     * @param query input query
     * @return table data after query execution
     */
    private Tables InsertIntoInMemoryStore(String query) {
        try {
            var tables = this._transactionManager.getTables();
            String[] queryArray = query.split("\\s+");
            // invalid query syntax
            if (!queryArray[1].equalsIgnoreCase("into")) {
                System.out.println("Check syntax");
                return null;
            }

            // validate and get table name
            String tableName = queryArray[2];
            if (!tableName.matches(QueryHelper.tableRegex) || !_StorageService.CheckDataStoreExists(tableName)) {
                System.out.println("No such table");
                return null;
            }

            // get table column names and validate
            Pattern pattern = Pattern.compile(QueryHelper.InsertQueryColumnRegex);
            Matcher matcher = pattern.matcher(query);
            if (!matcher.find() || matcher.groupCount() != 1) {
                System.out.println("Check syntax");
                return null;
            }

            var inputTableColumns = Arrays.asList(matcher.group(1).split(","));
            // get existing definition from table
            String tableDetail = _StorageService.ReadFirstLineFromDataStore(tableName);
            if (tableDetail == null) {
                System.out.println("System Unavailable");
                return null;
            }

            String[] tableDefinition = tableDetail.split(QueryHelper.columnSeparatorRegex);
            if (tableDefinition.length != inputTableColumns.size()) {
                System.out.println("Syntax error");
                return null;
            }

            inputTableColumns = inputTableColumns.stream().map(String::trim).toList();
            // Validate each column name with supplied column name in query(the order should also match)
            for (String tableDef : tableDefinition) {
                if (!inputTableColumns.contains(tableDef.trim().split(",")[0])) {
                    System.out.println("Syntax error");
                    return null;
                }
            }

            String remainingQuery = query.substring(query.indexOf(")")+1).trim();
            if (!remainingQuery.trim().startsWith("value")) {
                System.out.println("Syntax error");
                return null;
            }

            // get values to insert and validate
            Pattern valuePattern = Pattern.compile(QueryHelper.tableDefinitionGroupRegex);
            Matcher valueMatcher = valuePattern.matcher(remainingQuery);
            if (!valueMatcher.find() || valueMatcher.groupCount() != 1) {
                System.out.println("Check syntax");
                return null;
            }

            var rowValues = valueMatcher.group(1);
            String deimitedValue = rowValues.replaceAll(",", QueryHelper.columnSplitter);

            tables.addRowToTable(tableName, deimitedValue);
            if(!this.AddColumnDefinitionForTable(tableName)){
              return null;
            }

            return this._transactionManager.getTables();
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Select query validation and parser
     * @param query input query
     * @return in memory table after query execution
     */
    private Tables SelectQueryHandler(String query){
        try {
            // get table column names and validate
            Pattern pattern = Pattern.compile(QueryHelper.SelectQueryRegex);
            Matcher matcher = pattern.matcher(query);
            if (!matcher.find()) {
                System.out.println("Check syntax");
                return null;
            }

            // columns to select from input query
            String columnToSelect = matcher.group(2);
            // get table name form query
            String extractTableName = query.substring(query.indexOf("from") +5).trim();
            String tableName = extractTableName.split(";")[0];
            if (!tableName.matches(QueryHelper.tableRegex) || !_StorageService.CheckDataStoreExists(tableName)) {
                System.out.println("No such table/check syntax");
                return null;
            }

            if(tableName.equals("logs")){
                System.out.println("You can't see logs");
                return null;
            }

            var tableDataFromFile = _StorageService.ReadDataStoreWithLocks(tableName);
            if(tableDataFromFile == null){
                return null;
            }

            // transform the column names for output
            TransformColumnNames(tableDataFromFile);

            if(columnToSelect.trim().equals("*")){
               PrintTableData(tableDataFromFile, tableName);
            }
            else{
               PrintSelectedColumns(tableDataFromFile, columnToSelect.replaceAll("\\s",""), tableName);
            }

            return this._transactionManager.getTables();
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Adds column definition as first line to data store for a table
     * @param tableName name of the table
     * @return true if added successfully else false
     */
    private boolean AddColumnDefinitionForTable(String tableName){
        if(!this._transactionManager.getTables().getColumnDetails().containsKey(tableName)){
            String firstLineForTable = _StorageService.ReadFirstLineFromDataStore(tableName);
            if(firstLineForTable != null){
               return this._transactionManager.getTables().addColumnDetailsForTable(tableName, firstLineForTable);
            }
            else{
                return false;
            }
        }

        return true;
    }

    /**
     * commit a transaction
     * @return true if no error encountered else false
     */
    private boolean commitTransaction(){
        var tableData = this._transactionManager.getTables();
        if(!writeTableDataToFiles(tableData)){
            return false;
        }
        this._transactionManager.commitTransaction();

        return true;
    }

    /**
     * Writes in memory table data to data store
     * @param tableData in memory table data
     * @return true if write operation was successful else false
     */
    private boolean writeTableDataToFiles(Tables tableData){
        for (Map.Entry<String, Rows> entry : tableData.getTableDetails().entrySet()) {
            boolean entryInserted = _StorageService.WriteWithLock(entry.getKey(), entry.getValue().getRows());
            if(!entryInserted){
                return false;
            }
            // once all entries for a table is inserted into file, remove it from Transaction storage
            _transactionManager.getTables().removeTableDetailFromSet(entry.getKey());
        }

        return true;
    }

    /**
     * gets the column names from table columnName and definition string
     * @param tableData all data of the table
     */
    private void TransformColumnNames(String[] tableData){
        String[] firstRow = tableData[0].split(QueryHelper.columnSeparatorRegex);
        StringBuilder outputRow = new StringBuilder();
        for(int i = 0; i< firstRow.length; i++){
            outputRow.append(firstRow[i].split(",")[0].trim());
            if(i != firstRow.length-1) {
                outputRow.append(", ");
            }
        }

       tableData[0] = outputRow.toString();
    }

    /**
     * Matches and prints data from rows based on supplied column names
     * @param tableFileData persistent storage data
     * @param columnsToSelect columns to select
     * @param tableName table name
     */
    private void PrintSelectedColumns(String[] tableFileData, String columnsToSelect, String tableName){
        String[] firstRow = tableFileData[0].split(",");
        int totalColumnCount = firstRow.length;
        List<String> columnsSelected = Arrays.asList(columnsToSelect.split(","));
        Map<Integer, Integer> columnsToSkip = new HashMap<>(totalColumnCount);
        for(int i =0; i < totalColumnCount; i++){
            if(!columnsSelected.contains(firstRow[i].trim())){
                columnsToSkip.put(i,i);
            }
        }

        List<String> outputRows = new ArrayList<>();
        for(int i = 1; i< tableFileData.length; i++){
            StringBuilder outputRow = new StringBuilder();
            String[] rowData = tableFileData[i].split(QueryHelper.columnSeparatorRegex);
            for(int j = 0; j < rowData.length; j++){
                if(!columnsToSkip.containsKey(j)){
                    outputRow.append(rowData[j]);
                    outputRow.append(", ");
                }
            }
            outputRow.deleteCharAt(outputRow.lastIndexOf(","));
            outputRows.add(outputRow.toString());
        }

        if(this._transactionManager.getTransaction()){
            var inMemoryTable = this._transactionManager.getTables().getTable(tableName);
            if(inMemoryTable  != null){
                var inMemoryTableRows = inMemoryTable.getRows();
                for(String row: inMemoryTableRows){
                    StringBuilder outputRow = new StringBuilder();
                    String[] rowData = row.split(",");
                    for(int j = 0; j < rowData.length; j++){
                        if(!columnsToSkip.containsKey(j)){
                            outputRow.append(rowData[j]);
                            outputRow.append(", ");
                        }
                    }
                    outputRow.deleteCharAt(outputRow.lastIndexOf(","));
                    outputRows.add(outputRow.toString());
                }
            }

        }

        for(String row: outputRows){
            System.out.println(row);
        }
    }

    /**
     * Prints the table data
     * @param tableDataFromFile entire table data
     * @param tableName name of the table
     */
    private void PrintTableData(String[] tableDataFromFile, String tableName){
        for(String row: tableDataFromFile){
            System.out.println(row);
        }
        // in addition to data from file, get in memory data as well if it is in the transaction
        if(this._transactionManager.getTransaction()){
            var inMemoryTable = this._transactionManager.getTables().getTable(tableName);
            if(inMemoryTable  != null){
                for(String row: inMemoryTable.getRows()){
                    System.out.println(row);
                }
            }

        }
    }
}
