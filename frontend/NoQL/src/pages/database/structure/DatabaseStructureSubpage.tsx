import { Typography } from '@mui/material'
import { Database } from '../../../types/Database.ts'
import { useEffect, useState } from 'react'
import { DatabaseStructure, SqlDatabaseStructure } from '../../../types/DatabaseStructure.ts'
import databaseApi from '../../../services/api/databaseApi.ts'
import { SqlStructure } from './SqlStructure.tsx'
import { showErrorWithMessageAndError } from '../../../components/snackbar/GlobalSnackbar.helpers.ts'
import { AppDispatch } from '../../../state/store.ts'
import { useDispatch } from 'react-redux'
import { SkeletonStack } from '../../../components/loaders/SkeletonStack.tsx'

interface DatabaseStructureProps {
  databaseId: string,
  database: Database | null,
  databaseLoading: boolean,
}

export function DatabaseStructureSubpage({ databaseId, database, databaseLoading }: DatabaseStructureProps) {

  const dispatch: AppDispatch = useDispatch()

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
      showErrorWithMessageAndError(dispatch, 'Failed to load database structure', error)
    } finally {
      setDatabaseStructureLoading(false)
    }
  }

  const Structure =
    <>
      {database?.isSQL
        ? <SqlStructure structure={databaseStructure as SqlDatabaseStructure} />
        : <div>database engine not supported</div>
      }
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Structure</Typography>

      <Typography variant="h4" component="h2" sx={{ marginBottom: '2rem' }}>
        {databaseLoading ? 'Database' : database?.name}
      </Typography>

      { databaseStructureLoading
        ? <SkeletonStack height={24} count={5} />
        : Structure
      }
    </>
  )
}