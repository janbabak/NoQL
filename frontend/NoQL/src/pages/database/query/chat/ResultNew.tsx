import { ChatResponse } from '../../../../types/Chat.ts'
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
import { AppDispatch } from '../../../../state/store.ts'
import { useDispatch } from 'react-redux'
import { loadChatMessageData } from '../../../../state/chat/chatSlice.ts'
import React from 'react'

interface ResultProps {
  message: ChatResponse
  loading: boolean,
  paginationOptions?: number[]
}

export function ResultNew(
  {
    message,
    loading,
    paginationOptions = [10, 20, 25, 50]
  }: ResultProps) {

  const dispatch: AppDispatch = useDispatch()

  async function loadPage(page: number, pageSize: number): Promise<void> {
    await dispatch(loadChatMessageData({ messageId: message.messageId, page, pageSize }))
  }

  function onPageSizeChange(event: React.ChangeEvent<HTMLInputElement>): void {
    void loadPage(0, parseInt(event.target.value, 10))
  }

  function onPageChange(_event: unknown, newPage: number): void {
    void loadPage(newPage, message.data?.pageSize || 10)
  }

  const TableHeadElement =
    <TableHead>
      <TableRow>
        {message.data?.columnNames.map((columnName: string, columnNameIndex: number) =>
          <TableCell key={columnNameIndex}>{columnName}</TableCell>)}
      </TableRow>

      {loading &&
        <TableRow>
          <td colSpan={message.data?.columnNames.length}>
            <LinearProgress />
          </td>
        </TableRow>}
    </TableHead>

  const TableBodyElement =
    <TableBody>
      {message.data?.rows.map((row: string[], rowIndex: number) =>
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
          count={message.data?.totalCount || 0}
          rowsPerPage={message.data?.pageSize || 10}
          page={message.data?.page || 0}
          onPageChange={onPageChange}
          onRowsPerPageChange={onPageSizeChange}
        />
      </Paper>
    </Box>
  )
}