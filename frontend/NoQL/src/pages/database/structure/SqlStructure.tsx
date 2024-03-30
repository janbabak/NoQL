import { Column, SqlDatabaseStructure, Table } from '../../../types/DatabaseStructure.ts'
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView'
import { TreeItem } from '@mui/x-tree-view/TreeItem'
import { Schema } from '../../../types/DatabaseStructure.ts'
import { SqlColumn } from './SqlColumn.tsx'

interface SqlStructureProps {
  structure: SqlDatabaseStructure
}

export function SqlStructure({ structure }: SqlStructureProps) {
  return (
    <>
      <SimpleTreeView>
        {
          structure.schemas.map((schema: Schema, schemaIndex: number) => {
            return (
              <TreeItem
                itemId={'s' + schemaIndex}
                label={schema.name} key={schemaIndex}
              >
                {
                  schema.tables.map((table: Table, tableIndex: number) => {
                    return (
                      <TreeItem
                        itemId={'s' + schemaIndex + 't' + tableIndex}
                        label={table.name}
                        key={tableIndex}
                      >
                        {
                          table.columns.map((column: Column, columnIndex: number) => {
                            return <SqlColumn column={column} key={columnIndex} />
                          })
                        }
                      </TreeItem>
                    )
                  })
                }
              </TreeItem>
            )
          })
        }
      </SimpleTreeView>
    </>
  )
}