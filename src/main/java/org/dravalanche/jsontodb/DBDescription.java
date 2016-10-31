package org.dravalanche.jsontodb;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by slayer on 30.10.16.
 */
public class DBDescription {
    private List<TableDescription> tables;
    private TableDescription rootTableDescription;

    private Map<List<String>, TableDescription> tablesByPathMap = new HashMap<>();

    public DBDescription(List<TableDescription> tables
            , TableDescription rootTableDescription) {
        this.tables = tables;
        this.rootTableDescription = rootTableDescription;
    }

    public void print(PrintWriter writer) {
        writer.println("Tables:");
        for (TableDescription tableDescription : tables) {
            if (tableDescription.getParentTable() == null) {
                writer.println(tableDescription.getTableName() + "-->" + tableDescription.getJsonPath().stream().collect(Collectors.joining("/")));
            }else{
                writer.println(tableDescription.getTableName() + "-->" + tableDescription.getParentTable().getTableName() + " > " + tableDescription.getJsonPath().stream().collect(Collectors.joining("/")));
            }

            for (Column column : tableDescription.getColumns()) {
                writer.println("COLUMN>" + column.getColName() + " " + column.getSqlType() + " --> " + pathToStr(column.getColumnPath()) + " in " + pathToStr(tableDescription.getJsonPath()));
            }
        }
    }

    public void genSQL(PrintWriter writer) {

    }

    private String pathToStr(List<String> columnPath) {
        if (null == columnPath) {
            return "";
        }
        return columnPath.stream().collect(Collectors.joining("/"));
    }

    public List<TableDescription> getTables() {
        return tables;
    }


    public TableDescription getRootTableDescription() {
        return rootTableDescription;
    }
}
