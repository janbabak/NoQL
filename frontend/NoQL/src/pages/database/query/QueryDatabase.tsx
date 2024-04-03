import React, { useState } from 'react'
import { NATURAL_LANGUAGE_TAB } from './Constants.ts'
import { Box, Tab, Tabs, Typography } from '@mui/material'
import { Database } from '../../../types/Database.ts'
import { ChatTab } from './ChatTab.tsx'
import { ConsoleTab } from './ConsoleTab.tsx'

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

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  const TabsView =
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={tab}
          onChange={handleTabChange}
          sx={{ borderRadius: '0.25rem', marginTop: '1.5rem' }}
          aria-label="Chat and Console tabs"
        >
          <Tab label="Chat" sx={{ borderRadius: '0.25rem' }} />
          <Tab label="Editor" sx={{ borderRadius: '0.25rem' }} />
        </Tabs>
      </Box>

      <ChatTab databaseId={databaseId} tab={tab} />

      <ConsoleTab
        databaseId={databaseId}
        tab={tab}
        queryLanguageQuery={queryLanguageQuery}
        setQueryLanguageQuery={setQueryLanguageQuery}
      />
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>

      {!databaseLoading &&
        <>
          <Typography variant="h4" component="h2">{database?.name}</Typography>
          {TabsView}
        </>
      }
    </>
  )
}