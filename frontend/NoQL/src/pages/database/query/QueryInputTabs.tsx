import { Box, Tab, Tabs, TextField } from '@mui/material'
import styles from './Query.module.css'
import { QueryEditor } from './QueryEditor.tsx'
import React from 'react'
import { NATURAL_LANGUAGE_TAB, QUERY_LANGUAGE_TAB } from './Constants.ts'
import { Chat } from '../../../types/Query.ts'
import { ChatView } from './ChatView.tsx'
import SendRoundedIcon from '@mui/icons-material/SendRounded'
import { LoadingButton } from '@mui/lab'

interface Props {
  tab: number,
  setTab: React.Dispatch<React.SetStateAction<number>>,
  naturalLanguageQuery: React.MutableRefObject<string>,
  chat: Chat,
  queryLanguageQuery: string,
  setQueryLanguageQuery: React.Dispatch<React.SetStateAction<string>>,
  queryChat: () => void,
  queryLoading: boolean
}

export function QueryInputTabs(
  {
    tab,
    setTab,
    naturalLanguageQuery,
    chat,
    queryLanguageQuery,
    setQueryLanguageQuery,
    queryChat,
    queryLoading,
  }: Props) {

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  const NaturalLanguageTab =
    <div
      role="tabpanel"
      hidden={tab != NATURAL_LANGUAGE_TAB}
      className={styles.tabView}
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

    </div>

  const QueryLanguageTab =
    <div
      role="tabpanel"
      hidden={tab != QUERY_LANGUAGE_TAB}
      className={styles.tabView}
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
          <Tab label="Chat" sx={{ borderRadius: '0.25rem' }} />
          <Tab label="Editor" sx={{ borderRadius: '0.25rem' }} />
        </Tabs>
      </Box>

      {NaturalLanguageTab}
      {QueryLanguageTab}
    </>
  )
}