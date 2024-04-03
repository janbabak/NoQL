import { Box, Tab, Tabs } from '@mui/material'
import React from 'react'
import { ConsoleTab } from './ConsoleTab.tsx'
import { ChatTab } from './ChatTab.tsx'

interface Props {
  databaseId: string,
  tab: number,
  setTab: React.Dispatch<React.SetStateAction<number>>,
  queryLanguageQuery: string,
  setQueryLanguageQuery: React.Dispatch<React.SetStateAction<string>>,
}

export function QueryInputTabs(
  {
    databaseId,
    tab,
    setTab,
    queryLanguageQuery,
    setQueryLanguageQuery,
  }: Props) {

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={tab}
          aria-label="basic tabs example"
          onChange={handleTabChange}
          sx={{ borderRadius: '0.25rem', marginTop: '1.5rem' }}
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
  )
}