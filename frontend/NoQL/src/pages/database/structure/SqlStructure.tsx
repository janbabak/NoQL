import { SqlDatabaseStructure } from '../../../types/DatabaseStructure.ts'

interface SqlStructureProps {
  structure: SqlDatabaseStructure
}

export function SqlStructure({ structure }: SqlStructureProps) {
  return (
    <>
      {
        structure.schemas.map(schema => <div>{schema.name}</div>)
      }
    </>
  )
}