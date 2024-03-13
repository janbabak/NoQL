import { QueryResponse } from '../../types/QueryResponse.ts'
import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material'

export function ResultTable({ queryResult }: {
  queryResult?: QueryResponse
}) {
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
      </Table>
    </TableContainer>
  )
}