package org.dravalanche.jsontodb;

import javafx.scene.control.Tab;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by slayer on 30.10.16.
 */
public class JSONToDB {

    private final Set<String> skipOnObjectNames;

    private final String ID_COLUMN_NAME = "ID";
    private final String PARENT_ID_COLUMN_NAME = "PARENT_ID";

    private final Pattern illegalPatterns = Pattern.compile("[^a-z0-9]+");
    private final List<String> reservedColumnNames = Arrays.asList("ID", "PARENT_ID");

    private final DBDescription prevDBDescription;

    public JSONToDB(Set<String> skipOnObjectNames) {
        this.skipOnObjectNames = skipOnObjectNames;
        this.prevDBDescription = null;
    }

    public JSONToDB(Set<String> skipOnObjectNames, DBDescription prevDBDescription) {
        this.skipOnObjectNames = skipOnObjectNames;
        this.prevDBDescription = prevDBDescription;
    }

    public DBDescription getDBDescription(String baseElementName, JSONObject rootObject) {
        List<TableDescription> tables;
        TableDescription rootTableDescription;
        List<String> currentPath = new ArrayList<>();
        if (prevDBDescription != null) {
            tables = prevDBDescription.getTables();
            rootTableDescription = prevDBDescription.getRootTableDescription();
        } else {
            tables = new ArrayList<>();
            rootTableDescription = new TableDescription(Translit.toNormalizedTranslit(baseElementName), currentPath);
            tables.add(rootTableDescription);
        }

        visit(tables, rootTableDescription, rootObject, currentPath, null);

        fixColNamesNames(rootTableDescription);
        fixTableNames(tables);

        return new DBDescription(tables, rootTableDescription);
    }

    private void fixTableNames(List<TableDescription> tables) {
        NameAbbreviator abbreviator = NameAbbreviator.getAbbreviator("2$.2$");

        for (TableDescription table : tables) {
            if (table.getTableName().length() > 30) {
                StringBuffer buff = new StringBuffer();
                buff.append(table.getTableName());

                abbreviator.abbreviate(0, buff);

                String newValue = buff.toString();

                if (newValue.length() > 30) {
                    newValue = newValue.substring(newValue.length() - 30
                            , newValue.length());
                }

                table.setTableName(newValue.toUpperCase());
            } else {
                table.setTableName(table.getTableName().toUpperCase());
            }
        }

        while (true) {
            Map<String, List<TableDescription>> groupedNames = tables
                    .stream().collect(Collectors.groupingBy(TableDescription::getTableName));

            List<String> doubledValues = groupedNames.keySet()
                    .stream().filter(value -> groupedNames.get(value).size() > 1).collect(Collectors.toList());

            if (doubledValues.size() == 0) {
                break;
            }

            doubledValues.forEach(value -> {
                List<TableDescription> doubledTables = groupedNames.get(value);
                int count = doubledTables.size();
                int countLength = ("" + count).length();

                String base = value;

                if (base.length() + 1 + countLength > 30) {
                    base = base.substring(base.length() - (base.length() + 1 + countLength - 30), base.length());
                }

                for (int i = 0; i < doubledTables.size(); i++) {
                    TableDescription table = doubledTables.get(i);
                    table.setTableName(base + "_" + i);
                }
            });
        }
    }

    private void fixColNamesNames(TableDescription rootTableDescription) {
        //Сначала уберем ID и Parent_ID
        NameAbbreviator abbreviator = NameAbbreviator.getAbbreviator("2$.2$");

        for (Column column : rootTableDescription.getColumns()) {
            if (reservedColumnNames.contains(column.getColName())) {
                column.setColName(column.getColName() + "_V");
            }
            if (column.getColName().length() > 30) {
                StringBuffer buff = new StringBuffer();
                buff.append(column.getColName());

                abbreviator.abbreviate(0, buff);

                String newValue = buff.toString();

                if (newValue.length() > 30) {
                    newValue = newValue.substring(newValue.length() - 30
                            , newValue.length());
                }

                column.setColName(newValue.toUpperCase());
            } else {
                column.setColName(column.getColName().toUpperCase());
            }
        }

        while (true) {
            Map<String, List<Column>> groupedNames = rootTableDescription.getColumns()
                    .stream().collect(Collectors.groupingBy(Column::getColName));

            List<String> doubledValues = groupedNames.keySet()
                    .stream().filter(value -> groupedNames.get(value).size() > 1).collect(Collectors.toList());

            if (doubledValues.size() == 0) {
                break;
            }

            doubledValues.forEach(value -> {
                List<Column> doubledColumns = groupedNames.get(value);
                int count = doubledColumns.size();
                int countLength = ("" + count).length();

                String base = value;

                if (base.length() + 1 + countLength > 30) {
                    base = base.substring(base.length() - (base.length() + 1 + countLength - 30), base.length());
                }

                for (int i = 0; i < doubledColumns.size(); i++) {
                    Column column = doubledColumns.get(i);
                    column.setColName(base + "_" + i);
                }
            });
        }

        rootTableDescription.getChildTablesList().forEach(tableDescription -> fixColNamesNames(tableDescription));
    }

    private void visit(List<TableDescription> tables
            , TableDescription currentTable
            , JSONObject currentObject
            , final List<String> path
            , final String namePath) {
        for (String key : currentObject.keySet()) {
            Object value = currentObject.get(key);

            if (null == value) {
                return;
            }

            if (currentObject.isNull(key)) {
                continue;
            }

            if (!(value instanceof JSONObject || value instanceof JSONArray)) {
                String colName;
                String preparedKey = Translit.toNormalizedTranslit(key);
                preparedKey = preparedKey.replaceAll("[^a-zA-Z0-9]+", "_");
                if (null == namePath) {
                    colName = preparedKey;
                } else {
                    colName = namePath + "_" + preparedKey;
                }

                List<String> columnPath = new ArrayList<String>();
                columnPath.addAll(path);
                columnPath.add(key);

                String columnType;

                if (value instanceof String) {
                    columnType = "varchar2(255 char)";
                } else if (value instanceof Number) {
                    columnType = "number";
                } else if (value instanceof Boolean) {
                    columnType = "number(1,0)";
                } else {
                    columnPath.addAll(path);
                    throw new IllegalArgumentException("Wrong attribute type " + value.getClass());
                }

                Column column = new Column(colName, columnPath, columnType, value.getClass());

                Column existingColumn;

                if (currentTable.getChildTablesByPaths().get(columnPath) != null) {
                    throw new IllegalArgumentException("Cannot change array to scalar value at path " + columnPath.toString());
                }

                if ((existingColumn = currentTable.getColumnsByPaths().get(columnPath)) != null) {
                    if (!existingColumn.getJsonClass().equals(column.getJsonClass())) {
                        if (!existingColumn.getSqlType().equals(column.getSqlType())) {
                            throw new IllegalArgumentException("Cannot merge different types on columns "
                                    + existingColumn.getJsonClass()
                                    + " and " + column.getJsonClass() + " at path " + columnPath.toString());
                        }
                    }
                } else {
                    currentTable.addColumn(column);
                    currentTable.getColumnsByPaths().put(columnPath, column);
                }
            } else {
                if (value instanceof JSONObject) {
                    List<String> columnPath = new ArrayList<String>();
                    columnPath.addAll(path);
                    columnPath.add(key);
                    String colName;
                    String preparedKey = Translit.toNormalizedTranslit(key);
                    preparedKey = preparedKey.replaceAll("[^a-zA-Z0-9]+", "_");
                    colName = preparedKey;
                    if (null != namePath && !skipOnObjectNames.contains(key)) {
                        colName = namePath + "_" + preparedKey;
                    }
                    visit(tables, currentTable, (JSONObject) value, columnPath, colName);
                } else {
                    JSONArray jsonArray = (JSONArray) value;
                    if (((JSONArray) value).length() == 0) {
                        continue;
                    }
                    List<String> columnPath = new ArrayList<String>();
                    columnPath.addAll(path);
                    columnPath.add(key);
                    String colName = null;
                    String preparedKey = Translit.toNormalizedTranslit(key);
                    preparedKey = preparedKey.replaceAll("[^a-zA-Z0-9]+", "_");
                    if (null != namePath) {
                        colName = namePath + "_" + preparedKey;
                    }else{
                        colName = preparedKey;
                    }

                    TableDescription arrayTable;
                    if ((arrayTable = currentTable.getChildTablesByPaths().get(columnPath)) == null) {
                        arrayTable = new TableDescription(currentTable.getTableName() + "_" + colName, columnPath, currentTable);
                        tables.add(arrayTable);

                        currentTable.addChildTable(arrayTable);
                        currentTable.getChildTablesByPaths().put(columnPath, arrayTable);
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject currentArrayObject = null;
                        if (jsonArray.get(i) instanceof JSONObject) {
                            currentArrayObject = jsonArray.getJSONObject(i);
                        }else {
                            currentArrayObject = new JSONObject();
                            currentArrayObject.put("value", jsonArray.get(i));
                        }

                        visit(tables, arrayTable, currentArrayObject, new ArrayList<>(), null);
                    }
                }
            }
        }
    }
}
