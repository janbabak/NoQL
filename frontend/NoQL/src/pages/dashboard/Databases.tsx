import { memo, useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import { DatabaseCard } from './DatabaseCard.tsx'
import databaseApi from '../../services/api/databaseApi.ts'
import { Button, Typography } from '@mui/material'
import styles from './Dashboard.module.css'
import { useDispatch } from 'react-redux'
import { AppDispatch } from '../../state/store.ts'
import { showErrorWithMessageAndError } from '../../components/snackbar/GlobalSnackbar.helpers.ts'
import { SkeletonStack } from '../../components/loaders/SkeletonStack.tsx'
import AddIcon from '@mui/icons-material/Add'
import { CreateDatabaseDialog } from './CreateDatabaseDialog.tsx'
import { localStorageService } from '../../services/LocalStorageService.ts'

const Databases = memo(() => {

  const dispatch: AppDispatch = useDispatch()

  const [
    databases,
    setDatabases
  ] = useState<Database[]>([])

  const [
    databasesLoading,
    setDatabasesLoading
  ] = useState<boolean>(false)

  const [
    createDatabaseDialogOpen,
    setCreateDatabaseDialogOpen
  ] = useState<boolean>(false)

  useEffect((): void => {
    void loadDatabases()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Load list of databases from the backend
  async function loadDatabases(): Promise<void> {
    setDatabasesLoading(true)
    try {
      const response = await databaseApi.getAll(localStorageService.getUserId())
      setDatabases(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to load databases', error)
    } finally {
      setDatabasesLoading(false)
    }
  }

  const [
    deleteDatabaseLoading,
    setDeleteDatabaseLoading
  ] = useState<boolean>(false)

  async function deleteDatabase(databaseId: string): Promise<void> {
    setDeleteDatabaseLoading(true)
    try {
      await databaseApi.delete(databaseId)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to delete database', error)
    } finally {
      setDeleteDatabaseLoading(false)
    }
    void loadDatabases()
  }

  function openCreateDatabaseDialog(): void {
    setCreateDatabaseDialogOpen(true)
  }

  function closeCreateDatabaseDialog(): void {
    setCreateDatabaseDialogOpen(false)
    void loadDatabases()
  }

  const DatabasesList =
    <ul>{
      databases.map((db: Database) =>
        <DatabaseCard
          database={db}
          key={db.id}
          className={styles.databaseCard}
          deleteDatabase={deleteDatabase}
          deleteDatabaseLoading={deleteDatabaseLoading}
        />)
    }</ul>

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
        <Typography variant="h4" component="h2">
          Databases
        </Typography>

        <div>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDatabaseDialog}>
            Create
          </Button>
        </div>
      </div>

      <CreateDatabaseDialog
        open={createDatabaseDialogOpen}
        onClose={closeCreateDatabaseDialog}
      />

      {databasesLoading
        ? <SkeletonStack height={158} />
        : DatabasesList}
    </>
  )
})

export { Databases }