package org.dravalanche.jsontodb;

import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slayer on 30.10.16.
 */
public class TableDescription {
    private final List<String> jsonPath;
    private final List<Column> columns = new ArrayList<Column>();
    private final TableDescription parentTable;
    private String tableName;
    private final List<TableDescription> childTablesList = new ArrayList<TableDescription>();
    private final Map<List<String>, Column> columnsByPaths = new HashMap<>();
    private final Map<List<String>, TableDescription> childTablesByPaths = new HashMap<>();

    public TableDescription(String tableName, List<String> path) {
        this.jsonPath = path;
        this.parentTable = null;
        this.tableName = tableName;
    }

    public TableDescription(String tableName, List<String> path, TableDescription parentTable) {
        this.jsonPath = path;
        this.parentTable = parentTable;
        this.tableName = tableName;
    }

    public void addChildTable(TableDescription childTable) {
        this.childTablesList.add(childTable);
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public String getTableName() {
        return tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<String> getJsonPath() {
        return jsonPath;
    }

    public Map<List<String>, Column> getColumnsByPaths() {
        return columnsByPaths;
    }

    public Map<List<String>, TableDescription> getChildTablesByPaths() {
        return childTablesByPaths;
    }

    public List<TableDescription> getChildTablesList() {
        return childTablesList;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public TableDescription getParentTable() {
        return parentTable;
    }
}
