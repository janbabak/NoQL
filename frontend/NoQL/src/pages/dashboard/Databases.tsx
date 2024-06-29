import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import { DatabaseCard } from './DatabaseCard.tsx'
import databaseApi from '../../services/api/databaseApi.ts'
import { Typography } from '@mui/material'
import styles from './Dashboard.module.css'
import { useDispatch } from 'react-redux'
import { AppDispatch } from '../../state/store.ts'
import { showErrorWithMessageAndError } from '../../components/snackbar/GlobalSnackbar.helpers.ts'

export function Databases() {

  const dispatch: AppDispatch = useDispatch()

  const [
    databases,
    setDatabases
  ] = useState<Database[]>([])

  const [
    databasesLoading,
    setDatabasesLoading
  ] = useState<boolean>(false)

  useEffect((): void => {
    void loadDatabases()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Load list of databases from the backend
  async function loadDatabases(): Promise<void> {
    setDatabasesLoading(true)
    try {
      const response = await databaseApi.getAll()
      setDatabases(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, "Failed to load databases", error)
    } finally {
      setDatabasesLoading(false)
    }
  }

  const Loading = <div>loading ...</div> // TODO: loading

  const DatabasesList =
    <ul>{
      databases.map((db: Database) =>
        <DatabaseCard database={db} key={db.id} className={styles.databaseCard} />)
    }</ul>

  return (
    <>
      <Typography variant="h4" component="h2">Databases</Typography>
      {databasesLoading ? Loading : DatabasesList}
    </>
  )
}