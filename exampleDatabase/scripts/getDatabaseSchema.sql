-- get all tables from public schema, their columns and data types
SELECT table_schema, table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema NOT LIKE 'pg_%'
  AND table_schema != 'information_schema'
  AND table_name IN (SELECT table_name
                     FROM information_schema.tables
                     WHERE table_type = 'BASE TABLE'
                       AND table_catalog = current_database())
ORDER BY table_schema, table_name, ordinal_position;

-- get all schemas
SELECT schema_name
FROM information_schema.schemata;

-- get all schemas, where user defined tables can be found
SELECT schema_name
FROM information_schema.schemata
WHERE schema_name NOT LIKE 'pg_%'
  AND schema_name != 'information_schema';


-- get all tables from all schemas
SELECT table_name
FROM information_schema.tables;