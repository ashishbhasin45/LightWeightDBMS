package Assignment1.Helpers;

import java.util.HashMap;

/**
 * Contains all the static Strings to help with the processing and parsing of a query
 */
public class QueryHelper {
    public static final String insertQueryMatcher = "insert";
    public static final String selectQueryMatcher = "select";
    public static final String createQueryMatcher = "create";
    public static final String beginTransaction = "begin transaction;";
    public static final String commitTransaction = "commit transaction;";
    public static final String rollbackTransaction = "rollback transaction;";
    public static final String tableRegex ="[A-Za-z]+_?[A-Za-z]+";
    public static final String SelectQueryRegex ="^(select)(.*?)(from)";
    public static final String tableDefinitionGroupRegex = "\\((.*?)\\);";
    public static final String columnSplitter = "|";
    public static final String columnSeparatorRegex = "\\|";
    public static final String InsertQueryColumnRegex = "\\((.*?)\\)";

    public static String varcharMatcher = "varchar\\([1-9]\\d*\\)$";
    public static String decimalMatcher = "decimal";
    public static HashMap<String, String> allowedDataType = new HashMap<>(5);

    static {
        allowedDataType.put("int", "Int");
        allowedDataType.put("bigint", "Long");
        allowedDataType.put("varchar", "String");
        allowedDataType.put("datetime", "DateTime");
        allowedDataType.put("decimal", "Float");
    }

    public static String validateAndReturnType(String dataType){
        if(allowedDataType.containsKey(dataType)){
            return allowedDataType.get(dataType);
        }

        if(dataType.matches(varcharMatcher)){
            return allowedDataType.get("varchar");
        }

        if(dataType.matches(decimalMatcher)){
            return allowedDataType.get("decimal");
        }

        return null;
    }
}
