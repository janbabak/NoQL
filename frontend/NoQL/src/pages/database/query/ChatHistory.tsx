import styles from './Query.module.css'
import { Chat, ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { AxiosResponse } from 'axios'
import { LoadingButton } from '@mui/lab'
import { Button, CircularProgress, Menu, MenuItem, TextField } from '@mui/material'
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded'
import IconButton from '@mui/material/IconButton'
import React, { useEffect, useRef, useState } from 'react'
import { ConfirmDialog } from '../../../components/ConfirmDialog.tsx'
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded'
import EditRoundedIcon from '@mui/icons-material/EditRounded'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import { useDispatch, useSelector } from 'react-redux'
import { RootState } from '../../../state/store.ts'
import { addElement } from '../../../state/chatHistory/chatHistorySlice.ts'

interface ChatHistoryProps {
  chatHistory: ChatHistoryItem[],
  chatHistoryLoading: boolean,
  createChat: () => Promise<AxiosResponse<Chat>>,
  createChatLoading: boolean
  openChat: (id: string, index: number) => void,
  reallyDeleteChat: (chatId: string) => Promise<void>,
  renameChat: (chatId: string, newName: string) => Promise<void>,
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
    renameChat,
    activeChatIndex
  }: ChatHistoryProps) {

  const chatHistoryRedux: ChatHistoryItem[] = useSelector((state: RootState) => {
    return state.chatHistoryReducer.chatHistory
  })
  const dispatch = useDispatch()

  const [
    menuAnchorEl,
    setMenuAnchorEl
  ] = useState<null | HTMLElement>(null)

  const menuOpened: boolean = Boolean(menuAnchorEl)

  const [
    confirmDialogOpen,
    setConfirmDialogOpen
  ] = useState<boolean>(false)

  const [
    confirmDeleteChatTitle,
    setConfirmDeleteChatTitle
  ] = useState<string>('')

  const [
    chatToEdit,
    setChatToEdit
  ] = useState<ChatHistoryItem | null>(null)

  const [
    newName,
    setNewName
  ] = useState<string>('')

  // don't use chatToEdit because there is an useEffect hook, that focuses name input when this value changes
  const [
    chatToRenameId,
    setChatToRenameId
  ] = useState<string | null>(null)

  const renameInputRef: React.MutableRefObject<HTMLInputElement | undefined> = useRef()

  // opens delete/rename menu
  function openMenuClick(event: React.MouseEvent<HTMLElement>, chat: ChatHistoryItem): void {
    event.stopPropagation()
    setConfirmDeleteChatTitle('Delete chat: "' + chat.name + '"')
    setChatToEdit(chat)
    setMenuAnchorEl(event.currentTarget)
  }

  function deleteChatMenuClick(): void {
    setConfirmDialogOpen(true)
    closeMenu()
  }

  function confirmDeleteChat(): void {
    if (chatToEdit) {
      void reallyDeleteChat(chatToEdit.id)
    }
  }

  function renameChatMenuClick(): void {
    closeMenu()
    if (chatToEdit) {
      setChatToRenameId(chatToEdit.id)
    }
  }

  // focus input element that is rendered when chatToRenameId changes
  useEffect((): void => {
    if (renameInputRef && renameInputRef.current) {
      const chatToRename: ChatHistoryItem | undefined = chatHistory.find((c: ChatHistoryItem): boolean => {
        return chatToRenameId ? c.id === chatToRenameId : false
      })
      setNewName(chatToRename ? chatToRename.name : '') // set the old name
      renameInputRef.current.focus()
    }
  }, [chatToRenameId, chatHistory])

  function renameChatOnBlur(event: React.FocusEvent<HTMLInputElement>): void {
    reallyRenameChat(event.target.value)
  }

  function renameChatOnEnterPress(event: React.KeyboardEvent<HTMLDivElement>): void {
    if (event.key === 'Enter') {
      reallyRenameChat(newName)
    }
  }

  function reallyRenameChat(newName: string): void {
    if (chatToRenameId && newName) {
      void renameChat(chatToRenameId, newName)
    }
    setChatToRenameId(null)
  }

  function closeMenu(): void {
    setMenuAnchorEl(null)
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
      anchorEl={menuAnchorEl}
      open={menuOpened}
      onClose={closeMenu}
    >
      <MenuItem onClick={deleteChatMenuClick}>
        <ListItemIcon>
          <DeleteRoundedIcon fontSize="small" />
        </ListItemIcon>
        <ListItemText>Delete</ListItemText>
      </MenuItem>

      <MenuItem onClick={renameChatMenuClick}>
        <ListItemIcon>
          <EditRoundedIcon fontSize="small" />
        </ListItemIcon>
        <ListItemText>Rename</ListItemText>
      </MenuItem>
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
      <Button onClick={() => {
        dispatch(addElement())
        console.log("state")
        console.log(chatHistoryRedux)
      }}>print chat</Button>

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
                    {
                      chatToRenameId === chat.id
                        ? // rename input field
                        <TextField
                          onChange={(event) => setNewName(event.target.value)}
                          onBlur={renameChatOnBlur}
                          value={newName}
                          variant="standard"
                          size="small"
                          inputRef={renameInputRef}
                          onKeyDown={renameChatOnEnterPress}
                        />
                        : // name
                        <>
                          <span className={styles.chatHistoryItemLabel}>{chat.name}</span>
                          <IconButton
                            onClick={(event) => openMenuClick(event, chat)}
                            className={styles.chatHistoryItemIcon}
                            size="small"
                          >
                            <MoreHorizRoundedIcon fontSize="inherit" />
                          </IconButton>
                        </>
                    }
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