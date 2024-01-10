package com.janbabak.noqlbackend.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Schema {
    private String name;
    private Map<String, Table> tables;

    public Schema(String name) {
        this.name = name;
        this.tables = new HashMap<>();
    }

//    @Override
//    public String toString() {
//        return "schema \"" + name + "\" contains the following tables: " +
//                ""
//    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Table {
        private String name;
        private List<Column> columns;

        public Table(String name) {
            this.name = name;
            this.columns = new ArrayList<>();
        }
    }

    @Data
    @AllArgsConstructor
    public static class Column {
        private String name;
        private String dataType;
    }
}
