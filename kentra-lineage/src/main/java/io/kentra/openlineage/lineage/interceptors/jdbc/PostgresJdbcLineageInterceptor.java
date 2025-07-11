package io.kentra.openlineage.lineage.interceptors.jdbc;

import io.kentra.openlineage.lineage.LineageRegistry;
import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.Dataset;
import io.kentra.openlineage.lineage.model.Lineage;
import io.kentra.openlineage.lineage.model.Node;
import io.kentra.openlineage.lineage.model.PostgresDataset;
import io.openlineage.sql.DbTableMeta;
import io.openlineage.sql.OpenLineageSql;
import io.openlineage.sql.SqlMeta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * for now just a wrapper around OpenLineageSql
 */
public class PostgresJdbcLineageInterceptor {
  private LineageRegistry lineageRegistry;

  public PostgresJdbcLineageInterceptor(LineageRegistry lineageRegistry) {
    this.lineageRegistry = lineageRegistry;
  }

  public void registerJdbcLineage(String dbUrl, String sql) {
    Node node = NodeMdcUtil.getFromMdc();
    if (node == null) {
      return;
    }
    OpenLineageSql.parse(List.of(sql))
        .map(it -> mapToLineage(node, dbUrl, it))
        .ifPresent(lineageRegistry::registerLineage);
  }

  private Lineage mapToLineage(Node node, String dbUrl, SqlMeta sqlMeta) {
    Set<Dataset> inputs = toPostgresDataset(dbUrl, sqlMeta.inTables());
    Set<Dataset> outputs = toPostgresDataset(dbUrl, sqlMeta.outTables());
    return new Lineage(node, inputs, outputs);
  }

  private static Set<Dataset> toPostgresDataset(String dbUrl, List<DbTableMeta> sqlMeta) {
    return sqlMeta.stream()
        .map(it -> PostgresDataset.from(dbUrl, it))
        .collect(Collectors.toSet());
  }

}
