import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography
} from '@mui/material'
import { LoadingButton } from '@mui/lab'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { atomDark } from 'react-syntax-highlighter/dist/esm/styles/prism'
import styles from './Database.module.css'

export function DatabasePage() {
  const { id } = useParams<string>()
  const [database, setDatabase] = useState<Database | null>(null)
  const [databaseLoading, setDatabaseLoading] = useState<boolean>(false)
  const [queryResult, setQueryResult] = useState<QueryResponse | null>(
    {
      'result': {
        'columnNames': ['id', 'name', 'age', 'sex', 'email', 'created_at'],
        'rows': [['1', 'John Doe', '25', 'M ', 'john.doe@example.com', '2024-02-11 17:16:18.100286'], ['2', 'Jane Smith', '30', 'F ', 'jane.smith@example.com', '2024-02-11 17:16:18.100286'], ['3', 'Jane Doe', '28', 'F ', 'jane.doe@example.com', '2024-02-11 17:16:18.100286'], ['4', 'Bob Smith', '35', 'M ', 'bob.smith@example.com', '2024-02-11 17:16:18.100286'], ['5', 'Emily Johnson', '40', 'F ', 'emily.johnson@example.com', '2024-02-11 17:16:18.100286'], ['6', 'Michael Davis', '22', 'M ', 'michael.davis@example.com', '2024-02-11 17:16:18.100286'], ['7', 'Sarah Brown', '29', 'F ', 'sarah.brown@example.com', '2024-02-11 17:16:18.100286'], ['8', 'James Wilson', '33', 'M ', 'james.wilson@example.com', '2024-02-11 17:16:18.100286'], ['9', 'Jessica Lee', '26', 'F ', 'jessica.lee@example.com', '2024-02-11 17:16:18.100286'], ['10', 'David Taylor', '45', 'M ', 'david.taylor@example.com', '2024-02-11 17:16:18.100286'], ['11', 'Amanda Martinez', '31', 'F ', 'amanda.martinez@example.com', '2024-02-11 17:16:18.100286'], ['12', 'Daniel Miller', '27', 'M ', 'daniel.miller@example.com', '2024-02-11 17:16:18.100286'], ['13', 'Olivia Garcia', '38', 'F ', 'olivia.garcia@example.com', '2024-02-11 17:16:18.100286'], ['14', 'Matthew Hernandez', '23', 'M ', 'matthew.hernandez@example.com', '2024-02-11 17:16:18.100286'], ['15', 'Sophia Lopez', '32', 'F ', 'sophia.lopez@example.com', '2024-02-11 17:16:18.100286'], ['16', 'Andrew Young', '36', 'M ', 'andrew.young@example.com', '2024-02-11 17:16:18.100286'], ['17', 'Emma Scott', '30', 'F ', 'emma.scott@example.com', '2024-02-11 17:16:18.100286'], ['18', 'William Davis', '41', 'M ', 'william.davis@example.com', '2024-02-11 17:16:18.100286'], ['19', 'Ella Thomas', '24', 'F ', 'ella.thomas@example.com', '2024-02-11 17:16:18.100286'], ['20', 'Christopher Johnson', '37', 'M ', 'christopher.johnson@example.com', '2024-02-11 17:16:18.100286'], ['21', 'Grace Miller', '34', 'F ', 'grace.miller@example.com', '2024-02-11 17:16:18.100286'], ['22', 'Nicholas Brown', '39', 'M ', 'nicholas.brown@example.com', '2024-02-11 17:16:18.100286']]
      }, 'query': 'SELECT * FROM public.user;'
    }
  )
  const [queryLoading, setQueryLoading] = useState<boolean>(false)
  const usersQuery = useRef<string>('')

  useEffect(() => {
    loadDatabase()
  })

  // load database from API
  async function loadDatabase() {
    setDatabaseLoading(true)
    try {
      const response = await databaseApi.getById(id || '')
      setDatabase(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setDatabaseLoading(false)
    }
  }

  // get query result
  async function queryDatabase() {
    setQueryLoading(true)
    try {
      const response = await databaseApi.queryNaturalLanguage(id || '', usersQuery.current.value)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  const generatedCode =
    <SyntaxHighlighter language="sql" style={atomDark} className={styles.generatedQuery}>
      {queryResult?.query}
    </SyntaxHighlighter>

  const dataTable =
    <TableContainer component={Paper} elevation={2}>
      <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
          <TableRow>
            {queryResult?.result.columnNames.map(columnName =>
              <TableCell>{columnName}</TableCell>)}
          </TableRow>
        </TableHead>

        <TableBody>
          {queryResult?.result.rows.map(row =>
            <TableRow sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
              {row.map(cell =>
                <TableCell component="td" scope="row">{cell}</TableCell>)}
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      <Typography variant="h4" component="h2">{database?.name}</Typography>

      <div className={styles.queryInput}>
        <TextField
          id="query"
          label="Query"
          variant="outlined"
          inputRef={usersQuery}
          fullWidth
        />
      </div>

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={queryDatabase}
        className={styles.queryButton}
      >Query</LoadingButton>

      {queryResult != null
        ? generatedCode
        : ''}
      {queryResult != null
        ? dataTable
        : ''}
    </>
  )
}