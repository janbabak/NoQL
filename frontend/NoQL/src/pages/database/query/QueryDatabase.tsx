import React, { memo, useState } from 'react'
import { CHAT_TAB, CONSOLE_TAB } from './Constants.ts'
import { Box, Tab, Tabs, Typography } from '@mui/material'
import { Database } from '../../../types/Database.ts'
import { ChatTab } from './ChatTab.tsx'
import { ConsoleTab } from './ConsoleTab.tsx'

interface QueryDatabaseProps {
  databaseId: string,
  database: Database | null,
  databaseLoading: boolean,
}

const QueryDatabase = memo(({ databaseId, database, databaseLoading }: QueryDatabaseProps) => {

  const [
    tab,
    setTab
  ] = useState<number>(CHAT_TAB)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

  function editInConsole(query: string): void {
    setQueryLanguageQuery(query)
    setTab(CONSOLE_TAB)
  }

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>

      {!databaseLoading &&
        <>
          <Typography variant="h4" component="h2">{database?.name}</Typography>

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

          <ChatTab
            databaseId={databaseId}
            tab={tab}
            editQueryInConsole={editInConsole}
          />

          <ConsoleTab
            databaseId={databaseId}
            tab={tab}
            queryLanguageQuery={queryLanguageQuery}
            setQueryLanguageQuery={setQueryLanguageQuery}
          />
        </>
      }
    </>
  )
})

export { QueryDatabase }