import { ChatResponseData } from '../../../../types/Chat.ts'
import {
  Box,
  LinearProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow
} from '@mui/material'

interface ResultProps {
  data: ChatResponseData
  loading: boolean,
  paginationOptions?: number[]
}

export function ResultNew(
  {
    data,
    loading,
    paginationOptions = [10, 25, 50]
  }: ResultProps) {

  const TableHeadElement =
    <TableHead>
      <TableRow>
        {data.columnNames.map((columnName: string, columnNameIndex: number) =>
          <TableCell key={columnNameIndex}>{columnName}</TableCell>)}
      </TableRow>

      {loading &&
        <TableRow>
          <td colSpan={data.columnNames.length}>
            <LinearProgress />
          </td>
        </TableRow>}
    </TableHead>

  const TableBodyElement =
    <TableBody>
      {data.rows.map((row: string[], rowIndex: number) =>
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
          count={data.totalCount}
          rowsPerPage={data.pageSize}
          page={data.page}
          onPageChange={(_event) => console.log('implement')}
          onRowsPerPageChange={(_event) => console.log('implement')}
        />
      </Paper>
    </Box>
  )
}