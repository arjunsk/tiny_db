package com.arjunsk.tiny_db.server.a_frontend.common.domain.commands;

import com.arjunsk.tiny_db.server.a_frontend.common.domain.clause.A_Predicate;
import lombok.ToString;

import java.util.List;

/**
 * Data for the SQL <i>select</i> statement.
 *
 * @author Edward Sciore
 */
@ToString
public class QueryData {
    private List<String> fields;
    private String table;
    private A_Predicate pred;

    /**
     * Saves the field and table list and predicate.
     */
    public QueryData(List<String> fields, String table, A_Predicate pred) {
        this.fields = fields;
        this.table = table;
        this.pred = pred;
    }

    /**
     * Returns the fields mentioned in the select clause.
     *
     * @return a list of field names
     */
    public List<String> fields() {
        return fields;
    }

    /**
     * Returns the tables mentioned in the from clause.
     *
     * @return a collection of table names
     */
    public String table() {
        return table;
    }

    /**
     * Returns the predicate that describes which
     * records should be in the output table.
     *
     * @return the query predicate
     */
    public A_Predicate pred() {
        return pred;
    }

}
//DONE