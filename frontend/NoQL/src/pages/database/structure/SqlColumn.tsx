import { Column } from '../../../types/DatabaseStructure.ts'
import KeyRoundedIcon from '@mui/icons-material/KeyRounded'

interface SqlColumnProps {
  column: Column
}

export function SqlColumn({ column }: SqlColumnProps) {
  const PrimaryKey = <KeyRoundedIcon style={{ color: 'orange', marginLeft: '1rem'}} />
  const ForeignKey =
    <>
      {column.foreignKey != null &&
        <>
          <KeyRoundedIcon style={{ color: 'blue' }} />
          <span style={{ color: 'grey' }}>{column.foreignKey.referencedSchema + '.'
            + column.foreignKey.referencedTable + '.'
            + column.foreignKey.referencedColumn}</span>
        </>
      }
    </>

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'flex-start',
      alignItems: 'center',
      gap: '0.5rem',
      marginBottom: '0.25rem'
    }}>
      {column.isPrimaryKey && PrimaryKey}
      <span style={{
        marginLeft: column.isPrimaryKey ? '0' : '3rem'
      }}>{column.name}</span>
      {ForeignKey}
    </div>
  )
}