package org.dravalanche.jsontodb;

import java.util.List;

/**
 * Created by slayer on 30.10.16.
 */
public class Column {
    private String colName;
    private final List<String> columnPath;
    private final String sqlType;
    private final Class jsonClass;

    public Column(String colName, List<String> columnPath, String sqlType, Class typeClass) {
        this.colName = colName;
        this.columnPath = columnPath;
        this.sqlType = sqlType;
        this.jsonClass = typeClass;
    }

    public String getColName() {
        return colName;
    }

    public List<String> getColumnPath() {
        return columnPath;
    }

    public String getSqlType() {
        return sqlType;
    }

    public Class getJsonClass() {
        return jsonClass;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }
}
