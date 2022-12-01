package edu.utdallas.davisbase.server.b_query_engine.common.catalog;

import edu.utdallas.davisbase.server.b_query_engine.impl.basic.b_stats_manager.StatMgr;
import edu.utdallas.davisbase.server.b_query_engine.impl.basic.b_stats_manager.domain.StatInfo;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.index.IndexInfo;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.index.IndexMgr;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.table.TableMgr;
import edu.utdallas.davisbase.server.d_storage_engine.common.transaction.Transaction;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.table.TablePhysicalLayout;
import edu.utdallas.davisbase.server.b_query_engine.common.catalog.table.TableDefinition;

import java.util.Map;

/**
 * The Composite Manager for Index and Catalog
 *
 * @author Edward Sciore
 */
public class MetadataMgr {
    private static TableMgr tblmgr;
    private static IndexMgr idxmgr;
    private static StatMgr statmgr;

    public MetadataMgr(boolean isnew, Transaction tx) {
        tblmgr = new TableMgr(isnew, tx);
        statmgr = new StatMgr(tblmgr, tx);
        idxmgr = new IndexMgr(isnew, tblmgr, statmgr, tx);
    }

    public void createTable(String tblname, TableDefinition sch, Transaction tx) {
        tblmgr.createTable(tblname, sch, tx);
    }

    public TablePhysicalLayout getLayout(String tblname, Transaction tx) {
        return tblmgr.getLayout(tblname, tx);
    }


    public void createIndex(String idxname, String tblname, String fldname, Transaction tx) {
        idxmgr.createIndex(idxname, tblname, fldname, tx);
    }

    public Map<String, IndexInfo> getIndexInfo(String tblname, Transaction tx) {
        return idxmgr.getIndexInfo(tblname, tx);
    }

    public StatInfo getStatInfo(String tblname, TablePhysicalLayout layout, Transaction tx) {
        return statmgr.getStatInfo(tblname, layout, tx);
    }
}
