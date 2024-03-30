import { Box, Tab, Tabs, TextField } from '@mui/material'
import styles from '../Database.module.css'
import { QueryEditor } from './QueryEditor.tsx'
import React from 'react'
import { NATURAL_LANGUAGE_TAB, QUERY_LANGUAGE_TAB } from './Constants.ts'

interface Props {
  tab: number,
  setTab: React.Dispatch<React.SetStateAction<number>>,
  naturalLanguageQuery: React.MutableRefObject<string>,
  queryLanguageQuery: string,
  setQueryLanguageQuery: React.Dispatch<React.SetStateAction<string>>
}

export function QueryInputTabs(
  {
    tab,
    setTab,
    naturalLanguageQuery,
    queryLanguageQuery,
    setQueryLanguageQuery
  }: Props) {

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  const NaturalLanguageTab =
    <div
      role="tabpanel"
      hidden={tab != NATURAL_LANGUAGE_TAB}
      className={styles.queryInput}
    >
      <TextField
        id="query"
        label="Query"
        variant="outlined"
        inputRef={naturalLanguageQuery}
        fullWidth
      />
    </div>

  const QueryLanguageTab =
    <div
      role="tabpanel"
      hidden={tab != QUERY_LANGUAGE_TAB}
      className={styles.queryEditor}
    >
      <QueryEditor value={queryLanguageQuery} setValue={setQueryLanguageQuery} />
    </div>

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={tab}
          aria-label="basic tabs example"
          onChange={handleTabChange}
          sx={{ borderRadius: '0.25rem', marginTop: '1.5rem' }}
        >
          <Tab label="Natural language" sx={{ borderRadius: '0.25rem' }} />
          <Tab label="Query language" sx={{ borderRadius: '0.25rem' }} />
        </Tabs>
      </Box>

      {NaturalLanguageTab}
      {QueryLanguageTab}
    </>
  )
}