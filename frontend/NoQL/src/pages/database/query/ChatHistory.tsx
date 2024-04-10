import styles from './Query.module.css'
import { Chat, ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { AxiosResponse } from 'axios'
import { LoadingButton } from '@mui/lab'
import { CircularProgress, Menu, MenuItem } from '@mui/material'
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded'
import IconButton from '@mui/material/IconButton'
import { useState } from 'react'

interface ChatHistoryProps {
  chatHistory: ChatHistoryItem[],
  chatHistoryLoading: boolean,
  createChat: () => Promise<AxiosResponse<Chat>>,
  createChatLoading: boolean
  openChat: (id: string, index: number) => void,
  activeChatIndex: number,
}

export function ChatHistory(
  {
    chatHistory,
    chatHistoryLoading,
    createChat,
    createChatLoading,
    openChat,
    activeChatIndex
  }: ChatHistoryProps) {

  const [
    menuOpened,
    setMenuOpened
  ] = useState<boolean>(false)

  const [
    anchorEl,
    setAnchorEl
  ] = useState<null | HTMLElement>(null)

  const open = Boolean(anchorEl)

  const handleClick = (event: React.MouseEvent<HTMLElement>): void => {
    event.stopPropagation()
    setMenuOpened(true)
    setAnchorEl(event.currentTarget)
  }

  const handleClose = () => {
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

  return (
    <div className={styles.chatHistory}>
      {CreateNewChatButton}

      <div className={styles.chatHistoryList}>
        {chatHistoryLoading && ChatHistoryLoading}

        {!chatHistoryLoading && <div>
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
                    onClick={handleClick}
                    className={styles.chatHistoryItemIcon}
                    size="small"
                  >
                    <MoreHorizRoundedIcon fontSize="inherit" />
                  </IconButton>
                </div>
              )
            })
          }
          <Menu
            id="fade-menu"
            MenuListProps={{
              'aria-labelledby': 'fade-button'
            }}
            anchorEl={anchorEl}
            open={open}
            onClose={handleClose}
          >
            <MenuItem onClick={() => console.log('delete item')}>Delete</MenuItem>
            <MenuItem onClick={() => console.log('rename item')}>Rename</MenuItem>
          </Menu>
        </div>
        }
      </div>
    </div>
  )
}