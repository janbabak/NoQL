import { useRef, useState } from 'react'
import { Chat, QueryResponse } from '../../../types/Query.ts'
import { NATURAL_LANGUAGE_TAB } from './Constants.ts'
import databaseApi from '../../../services/api/databaseApi.ts'
import { Typography } from '@mui/material'
import { QueryInputTabs } from './QueryInputTabs.tsx'
import { LoadingButton } from '@mui/lab'
import styles from '../Database.module.css'
import { Result } from './Result.tsx'
import { Database } from '../../../types/Database.ts'

interface QueryDatabaseProps {
  databaseId: string,
  database: Database | null,
  databaseLoading: boolean,
}

export function QueryDatabase({ databaseId, database, databaseLoading }: QueryDatabaseProps) {

  const [
    queryResult,
    setQueryResult
  ] = useState<QueryResponse | null>(null)

  const [
    chat,
    setChat
  ] = useState<Chat>({ messages: [
    "find me all users",
      "SELECT * FROM public.user",
      "and sort them by their names",
      "SELECT * FROM public.user ORDER BY name",
    ] })

  const [
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  const [
    showGeneratedQuery,
    setShowGeneratedQuery
  ] = useState<boolean>(true)

  const [
    tab,
    setTab
  ] = useState<number>(NATURAL_LANGUAGE_TAB)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const [
    totalCount,
    setTotalCount
  ] = useState<number | null>(0)

  const naturalLanguageQuery = useRef<string>('')

  // get query result
  // async function queryDatabase(): Promise<void> {
  //   setPage(0)
  //   setQueryLoading(true)
  //   try {
  //     const naturalLanguage: boolean = tab == 0
  //     const response = naturalLanguage
  //       ? await databaseApi.queryNaturalLanguage(
  //         // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  //         // @ts-ignore
  //         databaseId, naturalLanguageQuery.current.value, pageSize)
  //       : await databaseApi.queryQueryLanguageQuery(
  //         databaseId, queryLanguageQuery, 0, pageSize)
  //     setQueryResult(response.data)
  //     setTotalCount(response.data.totalCount)
  //   } catch (error: unknown) {
  //     console.log(error) // TODO: handles
  //   } finally {
  //     setQueryLoading(false)
  //     setShowGeneratedQuery(true)
  //   }
  // }

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

      setChat({
        messages: [...newChat.messages, response.data.query]
      })

      setQueryResult(response.data)
      setTotalCount(response.data.totalCount)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
      setShowGeneratedQuery(true)
    }
  }

  // get next page
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

  function editQueryInEditor(query: string) {
    setQueryLanguageQuery(query)
    setTab(1)
    setShowGeneratedQuery(false)
  }

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>

      {!databaseLoading &&
        <>
          <Typography variant="h4" component="h2">{database?.name}</Typography>

          <QueryInputTabs
            tab={tab}
            setTab={setTab}
            naturalLanguageQuery={naturalLanguageQuery}
            chat={chat}
            queryLanguageQuery={queryLanguageQuery}
            setQueryLanguageQuery={setQueryLanguageQuery}
          />

          <LoadingButton
            loading={queryLoading}
            fullWidth
            variant="contained"
            onClick={queryChat}
            className={styles.queryButton}
          >Query</LoadingButton>

          <Result
            queryResponse={queryResult}
            showGeneratedQuery={showGeneratedQuery}
            editQueryInEditor={editQueryInEditor}
            page={page}
            pageSize={pageSize}
            setPageSize={setPageSize}
            totalCount={totalCount}
            onPageChange={onPageChange}
          />
        </>
      }
    </>
  )
}