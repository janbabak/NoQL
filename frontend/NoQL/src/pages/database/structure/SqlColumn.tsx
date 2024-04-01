import { Column, ForeignKey } from '../../../types/DatabaseStructure.ts'
import KeyRoundedIcon from '@mui/icons-material/KeyRounded'
import styles from './Structure.module.css'

interface SqlColumnProps {
  column: Column,
  openForeignKey: (foreignKey: ForeignKey) => void,
  id: string,
}

export function SqlColumn({ column, openForeignKey, id }: SqlColumnProps) {

  // format foreign column name to format schema.table.columnName
  function getFormattedColumnName(): string | null {
    if (!column.foreignKey) {
      return null
    }
    let name: string = column.foreignKey.referencedSchema + '.'

    // remove quotes from the table name if its surround by them
    if (column.foreignKey.referencedTable[0] == '"'
      && column.foreignKey.referencedTable[column.foreignKey.referencedTable.length - 1] == '"') {
      name += column.foreignKey.referencedTable.substring(1, column.foreignKey.referencedTable.length - 1)
    } else {
      name = column.foreignKey.referencedTable
    }

    return name + '.' + column.foreignKey.referencedColumn
  }

  const PrimaryKey = <KeyRoundedIcon className={styles.primaryKeyIcon} />

  const ForeignKey =
    <>
      {column.foreignKey != null &&
        <>
          <KeyRoundedIcon className={styles.foreignKeyIcon} />
          <a
            href={'#' + getFormattedColumnName()}
            onClick={() => openForeignKey(column.foreignKey)}
            className={styles.foreignKeyLabel}
          >
            {getFormattedColumnName()}
          </a>
        </>
      }
      <span className={styles.columnDataType}>
        {column.dataType.toUpperCase()}
      </span>
    </>

  return (
    <div className={styles.column} id={id}>
      {column.isPrimaryKey && PrimaryKey}
      <span
        style={{ marginLeft: column.isPrimaryKey ? '0' : '3rem' }}
        className={styles.columnLabel}
      >
        {column.name}
      </span>
      {ForeignKey}
    </div>
  )
}