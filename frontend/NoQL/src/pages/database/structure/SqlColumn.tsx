import { Column } from '../../../types/DatabaseStructure.ts'
import KeyRoundedIcon from '@mui/icons-material/KeyRounded'
import styles from './Structure.module.css'

interface SqlColumnProps {
  column: Column
}

export function SqlColumn({ column }: SqlColumnProps) {
  const PrimaryKey = <KeyRoundedIcon className={styles.primaryKeyIcon} />
  const ForeignKey =
    <>
      {column.foreignKey != null &&
        <>
          <KeyRoundedIcon className={styles.foreignKeyIcon} />
          <span className={styles.foreignKeyLabel}>
            {column.foreignKey.referencedSchema + '.'
              + column.foreignKey.referencedTable + '.'
              + column.foreignKey.referencedColumn}
          </span>
        </>
      }
      <span className={styles.columnDataType}>
        {column.dataType.toUpperCase()}
      </span>
    </>

  return (
    <div className={styles.column}>
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