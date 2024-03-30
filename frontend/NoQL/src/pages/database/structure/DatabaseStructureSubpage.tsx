import { Typography } from '@mui/material'
import { Database } from '../../../types/Database.ts'
import { useEffect, useState } from 'react'
import { DatabaseStructure, SqlDatabaseStructure } from '../../../types/DatabaseStructure.ts'
import databaseApi from '../../../services/api/databaseApi.ts'
import { SqlStructure } from './SqlStructure.tsx'

interface DatabaseStructureProps {
  databaseId: string,
  database: Database | null,
  databaseLoading: boolean,
}

export function DatabaseStructureSubpage({ databaseId, database, databaseLoading }: DatabaseStructureProps) {

  const [
    databaseStructure,
    setDatabaseStructure
  ] = useState<DatabaseStructure | null>(null)

  const [
    databaseStructureLoading,
    setDatabaseStructureLoading
  ] = useState<boolean>(false)

  useEffect((): void => {
    void loadDatabaseStructure()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // load database structure from API
  async function loadDatabaseStructure(): Promise<void> {
    setDatabaseStructureLoading(true)
    try {
      const response = await databaseApi.getStructure(databaseId)
      setDatabaseStructure(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setDatabaseStructureLoading(false)
    }
  }

  const Structure =
    <>
      { database?.isSQL
        ? <SqlStructure structure={databaseStructure as SqlDatabaseStructure} />
        : <div>database engine not supported</div>
      }
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Structure</Typography>

      {databaseLoading && <span>loading...</span>}
      {!databaseLoading &&
        <Typography variant="h4" component="h2" sx={{marginBottom: '2rem'}}>{database?.name}</Typography>
      }

      {!databaseStructureLoading && Structure}
    </>
  )
}