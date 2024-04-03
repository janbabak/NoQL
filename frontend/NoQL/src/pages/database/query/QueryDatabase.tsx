import { useState } from 'react'
import { NATURAL_LANGUAGE_TAB } from './Constants.ts'
import { Typography } from '@mui/material'
import { QueryInputTabs } from './QueryInputTabs.tsx'
import { Database } from '../../../types/Database.ts'

interface QueryDatabaseProps {
  databaseId: string,
  database: Database | null,
  databaseLoading: boolean,
}

export function QueryDatabase({ databaseId, database, databaseLoading }: QueryDatabaseProps) {

  const [
    tab,
    setTab
  ] = useState<number>(NATURAL_LANGUAGE_TAB)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

  function editQueryInEditor(query: string) {
    setQueryLanguageQuery(query)
    setTab(1)
  }

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>

      {!databaseLoading &&
        <>
          <Typography variant="h4" component="h2">{database?.name}</Typography>

          <QueryInputTabs
            databaseId={databaseId}
            tab={tab}
            setTab={setTab}
            queryLanguageQuery={queryLanguageQuery}
            setQueryLanguageQuery={setQueryLanguageQuery}
          />
        </>
      }
    </>
  )
}