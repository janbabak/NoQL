interface DatabaseStructure {}

interface SqlDatabaseStructure extends DatabaseStructure{
  schemas: Schema[],
}

interface Schema {
  name: string,
  tables: Table[],
}

interface Table {
  name: string,
  columns: Column[]
}

interface Column {
  name: string,
  dataType: string,
  isPrimaryKey: boolean,
  foreignKey: ForeignKey
}

interface ForeignKey {
  referencedSchema: string,
  referencedTable: string,
  referencedColumn: string,
}

export type { DatabaseStructure, SqlDatabaseStructure, Schema, Table, Column, ForeignKey }