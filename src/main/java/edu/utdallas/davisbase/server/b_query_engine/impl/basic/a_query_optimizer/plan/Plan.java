package edu.utdallas.davisbase.server.b_query_engine.impl.basic.a_query_optimizer.plan;

import edu.utdallas.davisbase.server.d_storage_engine.RORecordScan;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.table.TableDefinition;

/**
 * Plan encapsulate relational algebra and cost of operation.
 * This is passed on to Planner for getting an efficient physical plan.
 *
 * @author Edward Sciore, Arjun Sunil Kumar
 */
public interface Plan {

    RORecordScan open();

    TableDefinition schema();

    int blocksAccessed();
}
