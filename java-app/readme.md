## Workarounds
### Postgres default schema "public"
Default schema name can be defined in the jdbc connection string, but currently JdbcDatasetUtils don't support that. I'm
manually setting the schema as "public" if schema is missing in the intercepted sql statement.