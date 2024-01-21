package com.janbabak.noqlbackend.service;

/**
 * Responsible for creating the LLMs query content.
 */
public class QueryService {

    /**
     * Create query content for the LLM.
     *
     * @param naturalLanguageQuery query from the user/customer
     * @param dbStructure          structure of the database, e.g. create script (describes tables, columns, etc.)
     * @param dbEngine             name of the database engine (like postgres, mysql, etc.)
     * @param isSQL                true if the database is a dialect of SQL
     * @return generated query
     */
    public static String createQuery(String naturalLanguageQuery, String dbStructure, String dbEngine, Boolean isSQL) {
        StringBuilder queryBuilder = new StringBuilder();

        if (isSQL) {
            queryBuilder
                    .append("Write an SQL query for the ")
                    .append(dbEngine)
                    .append(" database, which does the following: ");
        } else {
            queryBuilder
                    .append("Write a query for the ")
                    .append(dbEngine)
                    .append("database.\n")
                    .append("It should do the following: ");
        }

        queryBuilder
                .append(naturalLanguageQuery)
                .append("\nThis is the database structure:\n")
                .append(dbStructure);

        return queryBuilder.toString();
    }
}
