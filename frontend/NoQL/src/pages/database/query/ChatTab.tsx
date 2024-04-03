import { NATURAL_LANGUAGE_TAB } from './Constants.ts'
import styles from './Query.module.css'
import { ChatView } from './ChatView.tsx'
import { TextField } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import SendRoundedIcon from '@mui/icons-material/SendRounded'
import React, { useRef, useState } from 'react'
import databaseApi from '../../../services/api/databaseApi.ts'
import { Chat, QueryResponse } from '../../../types/Query.ts'
import { Result } from './Result.tsx'

interface ChatTabProps {
  databaseId: string,
  tab: number,
}

export function ChatTab({ databaseId, tab }: ChatTabProps) {

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
  ] = useState<Chat>({ messages: [
      "find me all users",
      "SELECT * FROM public.user;",
      "and sort them by their names",
      "SELECT * FROM public.user\nORDER BY name;",
      "in descending order",
      "SELECT * FROM public.user\nORDER BY name DESC;",
      "show only name, age, email and sex columns",
      "SELECT name, age, email, sex FROM public.user\nORDER BY name DESC;",
      "make the name uppercase",
      "SELECT UPPER(name) AS name, age, email, sex\nFROM public.user\nORDER BY name DESC;"
    ] })

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const naturalLanguageQuery: React.MutableRefObject<string> = useRef<string>('')

  async function queryChat(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {

      const newChat = {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        messages: [...chat.messages, naturalLanguageQuery.current.value]
      }

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      naturalLanguageQuery.current.value = ''

      const response = await databaseApi.queryChat(
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        databaseId, newChat, pageSize)

      setChat({
        messages: [...newChat.messages, response.data.query]
      })

      setQueryResult(response.data)
      setTotalCount(response.data.totalCount)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
      // setShowGeneratedQuery(true)
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

  return (
    <div
      role="tabpanel"
      hidden={tab != NATURAL_LANGUAGE_TAB}
      className={styles.chatTab}
    >
      <ChatView chat={chat} />

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
        >Query</LoadingButton>
      </div>

      <Result
        queryResponse={queryResult}
        editQueryInEditor={
          (_: string) => console.log('not implemented')
        }
        page={page}
        pageSize={pageSize}
        setPageSize={setPageSize}
        totalCount={totalCount}
        onPageChange={onPageChange}
      />

    </div>
  )
}