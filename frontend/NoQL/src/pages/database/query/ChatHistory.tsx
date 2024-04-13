import styles from './Query.module.css'
import { Chat, ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { AxiosResponse } from 'axios'
import { LoadingButton } from '@mui/lab'
import { CircularProgress, Menu, MenuItem } from '@mui/material'
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded'
import IconButton from '@mui/material/IconButton'
import React, { useState } from 'react'
import { ConfirmDialog } from '../../../components/ConfirmDialog.tsx'

interface ChatHistoryProps {
  chatHistory: ChatHistoryItem[],
  chatHistoryLoading: boolean,
  createChat: () => Promise<AxiosResponse<Chat>>,
  createChatLoading: boolean
  openChat: (id: string, index: number) => void,
  reallyDeleteChat: (chatId: string) => Promise<void>,
  activeChatIndex: number,
}

export function ChatHistory(
  {
    chatHistory,
    chatHistoryLoading,
    createChat,
    createChatLoading,
    openChat,
    reallyDeleteChat, // when user confirms deletion
    activeChatIndex
  }: ChatHistoryProps) {

  const [
    anchorEl,
    setAnchorEl
  ] = useState<null | HTMLElement>(null)

  const menuOpened: boolean = Boolean(anchorEl)

  const [
    confirmDialogOpen,
    setConfirmDialogOpen
  ] = useState<boolean>(false)

  const [
    confirmDeleteChatTitle,
    setConfirmDeleteChatTitle
  ] = useState<string>('')

  const [
    chatToDelete,
    setChatToDelete
  ] = useState<ChatHistoryItem | null>(null)

  // opens delete/rename menu
  function openMenuClick(event: React.MouseEvent<HTMLElement>, chat: ChatHistoryItem): void {
    event.stopPropagation()
    setConfirmDeleteChatTitle('Delete chat: "' + chat.name + '"')
    setChatToDelete(chat)
    setAnchorEl(event.currentTarget)
  }

  function deleteChat(): void {
    setConfirmDialogOpen(true)
    closeMenu()
  }

  function confirmDeleteChat(): void {
    if (chatToDelete) {
      void reallyDeleteChat(chatToDelete.id)
    }
  }

  const closeMenu = (): void => {
    setAnchorEl(null)
  }

  const CreateNewChatButton =
    <LoadingButton
      onClick={createChat}
      startIcon={<AddRoundedIcon />}
      variant="outlined"
      loading={createChatLoading}
      disabled={createChatLoading}
      fullWidth
    >
      New chat
    </LoadingButton>

  const ChatHistoryLoading =
    <div className={styles.chatHistoryLoading}>
      <CircularProgress />
    </div>

  const ChatMenu =
    <Menu
      id="fade-menu"
      MenuListProps={{
        'aria-labelledby': 'fade-button'
      }}
      anchorEl={anchorEl}
      open={menuOpened}
      onClose={closeMenu}
    >
      <MenuItem onClick={deleteChat}>Delete</MenuItem>
      <MenuItem onClick={() => console.log('rename item')}>Rename</MenuItem>
    </Menu>

  const ConfirmDeleteDialog =
    <ConfirmDialog
      title={confirmDeleteChatTitle}
      open={confirmDialogOpen}
      setOpen={setConfirmDialogOpen}
      confirm={confirmDeleteChat}
    />

  return (
    <div className={styles.chatHistory}>
      {CreateNewChatButton}

      <div className={styles.chatHistoryList}>
        {chatHistoryLoading && ChatHistoryLoading}

        {!chatHistoryLoading &&
          <div>
            {
              chatHistory.map((chat: ChatHistoryItem, index: number) => {
                return (
                  <div
                    onClick={() => openChat(chat.id, index)}
                    key={chat.id}
                    className={index == activeChatIndex ? styles.chatHistoryItemActive : styles.chatHistoryItem}
                  >
                    <span className={styles.chatHistoryItemLabel}>{chat.name}</span>
                    <IconButton
                      onClick={(event) => openMenuClick(event, chat)}
                      className={styles.chatHistoryItemIcon}
                      size="small"
                    >
                      <MoreHorizRoundedIcon fontSize="inherit" />
                    </IconButton>
                  </div>
                )
              })
            }

            {ChatMenu}
          </div>
        }

        {ConfirmDeleteDialog}
      </div>
    </div>
  )
}