import { Column, ForeignKey, SqlDatabaseStructure, Table } from '../../../types/DatabaseStructure.ts'
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView'
import { TreeItem } from '@mui/x-tree-view/TreeItem'
import { Schema } from '../../../types/DatabaseStructure.ts'
import { SqlColumn } from './SqlColumn.tsx'
import React, { useState } from 'react'
import { Alert, Button } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'
import StorageRoundedIcon from '@mui/icons-material/StorageRounded'
import BackupTableRoundedIcon from '@mui/icons-material/BackupTableRounded'
import styles from './Structure.module.css'

interface SqlStructureProps {
  structure: SqlDatabaseStructure
}

export function SqlStructure({ structure }: SqlStructureProps) {
  const [
    expandedItems,
    setExpandedItems
  ] = useState<string[]>(structure
    ? structure.schemas.map((schema: Schema) => schema.name)
    : [])

  function handleExpandedItemsChange(_event: React.SyntheticEvent, itemIds: string[]): void {
    setExpandedItems(itemIds)
  }

  // collapse all items in the tree view
  function collapseAll(): void {
    setExpandedItems([])
  }

  // expand all items in the tree view
  function expandAll(): void {
    const expanded: string[] = []

    structure.schemas.forEach((schema: Schema): void => {
      expanded.push(schema.name)

      schema.tables.forEach((table: Table): void => {
        expanded.push(schema.name + '.' + table.name)

        table.columns.forEach((column: Column): void => {
          expanded.push(schema.name + '.' + table.name + '.' + column.name)
        })
      })
    })

    setExpandedItems(expanded)
  }

  function openForeignKey(foreignKey: ForeignKey): void {
    let newExpandedItem: string = foreignKey.referencedSchema + '.'
    // remove quotes if the table is surrounded by them
    if (foreignKey.referencedTable[0] == '"'
      && foreignKey.referencedTable[foreignKey.referencedTable.length - 1] == '"') {
      newExpandedItem += foreignKey.referencedTable.substring(1, foreignKey.referencedTable.length - 1)
    } else {
      newExpandedItem += foreignKey.referencedTable
    }

    setExpandedItems([...new Set([...expandedItems, newExpandedItem, foreignKey.referencedSchema])])
  }

  const ExpansionButtons =
    <div className={styles.expansionButtons}>
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

  const TreeViewContent =
    <>
      {!structure
        ? <Alert severity="error">Failed to load database structure</Alert>
        : structure.schemas.map((schema: Schema, schemaIndex: number) => {
          return (
            <TreeItem
              itemId={schema.name}
              sx={{ marginBottom: '1rem' }}
              label={
                <span className={styles.treeItemLabel}>
                  <StorageRoundedIcon />
                  {schema.name}
                </span>}
              key={schemaIndex}
            >
              {
                schema.tables.map((table: Table, tableIndex: number) => {
                  return (
                    <TreeItem
                      itemId={schema.name + '.' + table.name}
                      label={
                        <span className={styles.treeItemLabel}>
                          <BackupTableRoundedIcon />
                          {table.name}
                        </span>}
                      key={tableIndex}
                    >
                      {
                        table.columns.map((column: Column, columnIndex: number) => {
                          return (
                            <SqlColumn
                              column={column}
                              key={columnIndex}
                              openForeignKey={openForeignKey}
                              id={schema.name + '.' + table.name + '.' + column.name}
                            />
                          )
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
    </>

  return (
    <>
      {ExpansionButtons}

      <SimpleTreeView
        expandedItems={expandedItems}
        onExpandedItemsChange={handleExpandedItemsChange}
      >
        {TreeViewContent}
      </SimpleTreeView>
    </>
  )
}