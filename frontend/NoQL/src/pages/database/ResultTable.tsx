import { QueryResponse } from '../../types/QueryResponse.ts'
import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow } from '@mui/material'
import { useState } from 'react'

export function ResultTable({ queryResult, onPageChange }: {
  queryResult?: QueryResponse,
  onPageChange: (page: number, pageSize: number) => void
}) {

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  function changePage(_event: unknown, newPage: number) {
    setPage(newPage)
    onPageChange(newPage, pageSize) // does not use pate because state updates on new render
  }

  function onRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    const newPageSize = parseInt(event.target.value, 10)
    setPageSize(newPageSize)
    setPage(0)
    onPageChange(0, newPageSize) // does not use page, pageSize because state updates on next render
  }

  // https://mui.com/material-ui/react-table/#data-table
  return (
    <TableContainer component={Paper} elevation={2}>
      <Table sx={{ minWidth: 650 }} aria-label="simple table">

        <TableHead>
          <TableRow>
            {queryResult?.result.columnNames.map((columnName, columnNameIndex: number) =>
              <TableCell key={columnNameIndex}>{columnName}</TableCell>)}
          </TableRow>
        </TableHead>

        <TableBody>
          {queryResult?.result.rows.map((row, rowIndex: number) =>
            <TableRow key={rowIndex} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
              {row.map((cell, cellIndex: number) =>
                <TableCell key={cellIndex} component="td" scope="row">{cell}</TableCell>)}
            </TableRow>
          )}
        </TableBody>

        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={50}
          rowsPerPage={pageSize}
          page={page}
          onPageChange={changePage}
          onRowsPerPageChange={onRowsPerPageChange}
        />
      </Table>
    </TableContainer>
  )
}