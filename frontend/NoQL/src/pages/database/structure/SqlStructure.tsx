import { Column, SqlDatabaseStructure, Table } from '../../../types/DatabaseStructure.ts'
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView'
import { TreeItem } from '@mui/x-tree-view/TreeItem'
import { Schema } from '../../../types/DatabaseStructure.ts'
import { SqlColumn } from './SqlColumn.tsx'
import { useState } from 'react'
import { Button } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'

interface SqlStructureProps {
  structure: SqlDatabaseStructure
}

export function SqlStructure({ structure }: SqlStructureProps) {
  const [
    expandedItems,
    setExpandedItems
  ] = useState<string[]>(structure.schemas.map((_: Schema, index: number) => 's' + index))

  function handleExpandedItemsChange(_event: React.SyntheticEvent, itemIds: string[]) {
    setExpandedItems(itemIds)
  }

  // expand all items in the tree view
  function expandAll(): void {
    const expanded: string[] = []

    structure.schemas.forEach((schema: Schema, schemaIndex: number): void => {
      expanded.push('s' + schemaIndex)

      schema.tables.forEach((table: Table, tableIndex: number): void => {
        expanded.push('s' + schemaIndex + 't' + tableIndex)

        table.columns.forEach((_: Column, columnIndex: number): void => {
          expanded.push('s' + schemaIndex + 't' + tableIndex + 'c' + columnIndex)
        })
      })
    })

    setExpandedItems(expanded)
  }

  // collapse all items in the tree view
  function collapseAll(): void {
    setExpandedItems([])
  }

  const ExpansionButtons =
    <div style={{marginBottom: '1rem'}}>
      <Button
        startIcon={<ExpandMoreIcon />}
        onClick={expandAll}
      >
        Expand all
      </Button>

      <Button
        startIcon={<ExpandLessIcon />}
        onClick={collapseAll}
      >
        Collapse all
      </Button>
    </div>

  return (
    <>
      {ExpansionButtons}

      <SimpleTreeView
        expandedItems={expandedItems}
        onExpandedItemsChange={handleExpandedItemsChange}
      >
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