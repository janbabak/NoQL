import { CHAT_TAB } from './Constants.ts'
import styles from './Query.module.css'
import { TextField } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import SendRoundedIcon from '@mui/icons-material/SendRounded'
import React, { useEffect, useRef, useState } from 'react'
import databaseApi from '../../../services/api/databaseApi.ts'
import { QueryResponse } from '../../../types/Query.ts'
import { Result } from './Result.tsx'
import { ChatHistory } from './ChatHistory.tsx'
import { ChatDto, ChatFromApi } from '../../../types/Chat.ts'
import { ChatView } from './ChatView.tsx'
import chatApi from '../../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatTabProps {
  databaseId: string,
  tab: number,
  editQueryInConsole: (query: string) => void,
}

export function ChatTab({ databaseId, tab, editQueryInConsole }: ChatTabProps) {

  const [
    queryResult,
    setQueryResult
  ] = useState<QueryResponse | null>(null)

  const [
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  const [
    totalCount,
    setTotalCount
  ] = useState<number | null>(0)

  const [
    chat,
    setChat
  ] = useState<ChatFromApi | null>(null)

  const [
    chatLoading,
    setChatLoading
  ] = useState<boolean>(false)

  const [
    chatHistory,
    setChatHistory
  ] = useState<ChatDto[]>([])

  const [
    chatHistoryLoading,
    setChatHistoryLoading
  ] = useState<boolean>(false)

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const naturalLanguageQuery: React.MutableRefObject<string> = useRef<string>('')

  useEffect((): void => {
    loadChatsHistory().then((response: AxiosResponse<ChatDto[], unknown>): void => {
      if (response.data.length > 0) {
        void loadChat(response.data[0].id)
      } else {
        console.log('create new chat not implemented') // TODO: implement
      }
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function loadChat(id: string): Promise<void> {
    setChatLoading(true)
    try {
      const response = await chatApi.getById(id)
      setChat(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setChatLoading(false)
    }
  }

  async function loadChatsHistory(): Promise<AxiosResponse<ChatDto[]>> {
    setChatHistoryLoading(true)
    try {
      const response = await databaseApi.getChatsFromDatabase(databaseId)
      setChatHistory(response.data)
      return response
    } catch (error: unknown) {
      console.log(error) // TODO: handle
      return Promise.reject(error)
    } finally {
      setChatHistoryLoading(false)
    }
  }

  async function queryChat(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {

      const newChat = {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        messages: [...chat.messages, naturalLanguageQuery.current.value]
      }

      const response = await databaseApi.queryChat(
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        databaseId, newChat, pageSize)

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      naturalLanguageQuery.current.value = ''

      // TODO
      // setChat({
      //   messages: [...newChat.messages, response.data.query]
      // })
      setQueryResult(response.data)
      setTotalCount(response.data.totalCount)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  async function onPageChange(page: number, pageSize: number): Promise<void> {
    setPageSize(pageSize)
    setPage(page)

    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        databaseId,
        queryResult?.query || '',
        page,
        pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  function openChat(id: string): void {
    // console.log('open chat' + id)
    loadChat(id)
  }

  return (
    <div
      role="tabpanel"
      hidden={tab != CHAT_TAB}
      className={styles.chatTab}
    >
      <div className={styles.chatTabContainer}>
        <ChatHistory
          chatHistory={chatHistory}
          chatHistoryLoading={chatHistoryLoading}
          refreshChatHistory={loadChatsHistory}
          databaseId={databaseId}
          openChat={openChat}
        />

        <div className={styles.chatWithInput}>

          <ChatView chat={chat} chatLoading={chatLoading} />

          <div className={styles.chatInputContainer}>
            <TextField
              id="query"
              label="Query"
              variant="standard"
              inputRef={naturalLanguageQuery}
              fullWidth
            />

            <LoadingButton
              loading={queryLoading}
              variant="contained"
              endIcon={<SendRoundedIcon />}
              onClick={queryChat}
            >
              Query
            </LoadingButton>
          </div>
        </div>
      </div>

      <Result
        queryResponse={queryResult}
        editQueryInConsole={editQueryInConsole}
        showEditInConsoleButton={true}
        page={page}
        pageSize={pageSize}
        setPageSize={setPageSize}
        totalCount={totalCount}
        onPageChange={onPageChange}
      />

    </div>
  )
}