package io.kentra.openlineage.lineage.model;

import io.openlineage.client.utils.jdbc.JdbcDatasetUtils;
import io.openlineage.sql.DbTableMeta;

public record PostgresDataset(
    String namespace,
    String name
) implements Dataset {
  public static PostgresDataset from(String dbUrl, DbTableMeta dbTableMeta) {
    String table;
    if (dbTableMeta.schema() == null) {
      table = "public." + dbTableMeta.name(); // workaround, check readme
    } else {
      table = dbTableMeta.schema() + "." + dbTableMeta.name();
    }
    var datasetIdentifier = JdbcDatasetUtils.getDatasetIdentifier(dbUrl, table, null);
    return new PostgresDataset(datasetIdentifier.getNamespace(), datasetIdentifier.getName());
  }
}
