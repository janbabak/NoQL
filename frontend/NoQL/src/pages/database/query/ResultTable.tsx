import { QueryResponse } from '../../../types/Query.ts'
import {
  Box, LinearProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow
} from '@mui/material'
import React from 'react'

interface Props {
  queryResult?: QueryResponse,
  page: number
  pageSize: number
  totalCount: number
  onPageChange: (page: number, pageSize: number) => void,
  onPageSizeChange: (newPageSize: number) => void,
  paginationOptions?: number[],
  loading: boolean,
}

export function ResultTable(
  {
    queryResult,
    page,
    pageSize,
    totalCount,
    onPageChange,
    onPageSizeChange,
    paginationOptions = [10, 25, 50],
    loading
  }: Props) {

  function changePage(_event: unknown, newPage: number): void {
    onPageChange(newPage, pageSize)
  }

  function onRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>): void {
    onPageSizeChange(parseInt(event.target.value, 10))
  }

  const TableHeadElement =
    <TableHead>
      <TableRow>
        {queryResult?.data?.columnNames.map((columnName, columnNameIndex: number) =>
          <TableCell key={columnNameIndex}>{columnName}</TableCell>)}
      </TableRow>

      {loading &&
        <TableRow>
          <td colSpan={queryResult?.data?.columnNames.length}>
            <LinearProgress />
          </td>
        </TableRow>}
    </TableHead>

  const TableBodyElement =
    <TableBody>
      {queryResult?.data?.rows.map((row, rowIndex: number) =>
        <TableRow key={rowIndex} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
          {row.map((cell, cellIndex: number) =>
            <TableCell key={cellIndex} component="td" scope="row">{cell}</TableCell>)}
        </TableRow>
      )}
    </TableBody>

  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ width: '100%', mb: 2 }}>
        <TableContainer>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            {TableHeadElement}
            {TableBodyElement}
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={paginationOptions}
          component="div"
          count={totalCount}
          rowsPerPage={pageSize}
          page={page}
          onPageChange={changePage}
          onRowsPerPageChange={onRowsPerPageChange}
        />
      </Paper>
    </Box>
  )
}