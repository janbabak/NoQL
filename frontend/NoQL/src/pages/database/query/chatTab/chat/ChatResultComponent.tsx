import { ChatResponse } from '../../../../../types/Chat.ts'
import { AppDispatch } from '../../../../../state/store.ts'
import { useDispatch } from 'react-redux'
import { loadChatMessageData } from '../../../../../state/chat/chatSlice.ts'
import { useState } from 'react'
import { ResultTable } from '../../ResultTable.tsx'

interface ResultProps {
  message: ChatResponse
  paginationOptions?: number[]
}

/**
 * Component for displaying a message result - table of data
 * @param message - chat message with data
 * @param paginationOptions - list of possible page sizes
 */
export function ChatResultComponent(
  {
    message,
    paginationOptions = [10, 20, 25, 50]
  }: ResultProps) {

  const dispatch: AppDispatch = useDispatch()

  const [
    loading,
    setLoading
  ] = useState<boolean>(false)

  async function loadPage(page: number, pageSize: number): Promise<void> {
    setLoading(true)
    try {
      await dispatch(loadChatMessageData({ messageId: message.messageId, page, pageSize }))
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {message.data &&
        <ResultTable
          data={message.data}
          onPageChange={loadPage}
          loading={loading}
          paginationOptions={paginationOptions}
        />
      }
    </>
  )
}