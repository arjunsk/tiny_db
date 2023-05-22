package com.arjunsk.tiny_db.server.b_query_engine.impl.calcite;

import com.arjunsk.tiny_db.server.b_query_engine.impl.calcite.core.B_Table;
import com.arjunsk.tiny_db.server.b_query_engine.impl.calcite.core.C_Schema;
import com.arjunsk.tiny_db.server.b_query_engine.impl.calcite.core.D_JavaSqlTypeToCalciteSqlTypeConversionRules;
import com.arjunsk.tiny_db.server.d_storage_engine.common.file.FileMgr;
import com.arjunsk.tiny_db.server.b_query_engine.IQueryEngine;
import com.arjunsk.tiny_db.server.b_query_engine.common.catalog.MetadataMgr;
import com.arjunsk.tiny_db.server.b_query_engine.common.catalog.table.TablePhysicalLayout;
import com.arjunsk.tiny_db.server.b_query_engine.common.dto.TableDto;
import com.arjunsk.tiny_db.server.d_storage_engine.common.transaction.Transaction;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * QueryEngine Implementation with Calcite Parser and Planner.
 * <p>
 * NOTE: There is no Optimizer implemented. Also, Since ModifiableTable is not implemented in
 * {@link  B_Table}, we currently don't support DML operations.
 *
 * @author Arjun Sunil Kumar
 */
public class CalciteQueryEngine implements IQueryEngine {

  public static int BLOCK_SIZE = 512;
  FileMgr fm;
  MetadataMgr mdm;
  Connection connection;

  String tableName = "T1";
  String schemaName = "tinydb";

  @SneakyThrows
  public CalciteQueryEngine(String dirname) {

    //1. Init MetaDataManager (Catalog)
    File dbDirectory = new File(dirname);
    fm = new FileMgr(dbDirectory, BLOCK_SIZE);
    Transaction tx1 = newTx();
    mdm = new MetadataMgr(fm.isNew(), tx1);
    tx1.commit();

    // 4.a JDBC similar
    Class.forName("org.apache.calcite.jdbc.Driver");
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    connection = DriverManager.getConnection("jdbc:calcite:", info);
  }


  /**
   * Use the Syntax:
   * <code>
   * select A,B from tinydb.T1;
   * </code>
   */
  @SneakyThrows
  public TableDto doQuery(String sql) {
    //2.a Get Table Layout
    Transaction tx2 = newTx();
    TablePhysicalLayout tableLayout = mdm.getLayout(tableName, tx2);

    // 2.b Create List<SqlType>
    D_JavaSqlTypeToCalciteSqlTypeConversionRules dataTypeRules = D_JavaSqlTypeToCalciteSqlTypeConversionRules.instance();
    List<SqlTypeName> fieldTypes = tableLayout.schema().fields().stream()
        .map(e -> tableLayout.schema().type(e)).map(dataTypeRules::lookup)
        .collect(Collectors.toList());

    // 2.c Create CalciteTable Object using fieldNames, fieldTypes etc
    B_Table calciteTable = new B_Table(tableName, tableLayout.schema().fields(), fieldTypes, tx2,
        mdm);

    // 3. Create Schema for the CalciteTable
    C_Schema schema = new C_Schema(Collections.singletonMap(tableName, calciteTable));

    // 4. Add schema to the SQL root schema

    // 4.b Unwrap and add proxy
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    rootSchema.add(schemaName, schema);

    // 5. Execute JDBC Query
    Statement statement = calciteConnection.createStatement();
    ResultSet rs = statement.executeQuery(sql);

    List<String> columnNames = tableLayout.schema().fields();
    List<List<String>> rows = new ArrayList<>();
    while (rs.next()) {
      List<String> row = new ArrayList<>();
      for (String field : columnNames) {
        row.add(rs.getString(field));
      }
      rows.add(row);
    }

    rs.close();
    statement.close();
    tx2.commit();

    return new TableDto(columnNames, rows);
  }

  @Override
  public TableDto doUpdate(String sql) {
    return null;
  }

  @SneakyThrows
  @Override
  public void close() {
    connection.close();
  }

  private Transaction newTx() {
    return new Transaction(fm);
  }

}
