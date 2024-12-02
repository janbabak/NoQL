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
import React, { memo } from 'react'
import { RetrievedData } from '../../../types/Query.ts'

interface Props {
  data?: RetrievedData,
  onPageChange: (page: number, pageSize: number) => void,
  paginationOptions?: number[],
  loading: boolean,
}

/**
 * Component for displaying a result - table of data.
 */
const ResultTable = memo((
  {
    data,
    onPageChange,
    paginationOptions = [10, 25, 50],
    loading
  }: Props) => {

  function changePage(_event: unknown, newPage: number): void {
    onPageChange(newPage, data?.pageSize || 10) // TODO: configure default page size?
  }

  function onRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>): void {
    onPageChange(0, parseInt(event.target.value, 10))
  }

  const TableHeadElement =
    <TableHead>
      <TableRow>
        {data?.columnNames.map((columnName: string, columnNameIndex: number) =>
          <TableCell key={columnNameIndex}>{columnName}</TableCell>)}
      </TableRow>

      {loading &&
        <TableRow>
          <td colSpan={data?.columnNames.length}>
            <LinearProgress />
          </td>
        </TableRow>}
    </TableHead>

  const TableBodyElement =
    <TableBody>
      {data?.rows.map((row: string[], rowIndex: number) =>
        <TableRow
          key={rowIndex}
          sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
        >
          {row.map((cell: string, cellIndex: number) =>
            <TableCell
              key={cellIndex}
              component="td"
              scope="row"
            >
              {cell}
            </TableCell>)}
        </TableRow>)}
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
          count={data?.totalCount || 0}
          rowsPerPage={data?.pageSize || 10}
          page={data?.page || 0}
          onPageChange={changePage}
          onRowsPerPageChange={onRowsPerPageChange}
        />
      </Paper>
    </Box>
  )
})

export { ResultTable }