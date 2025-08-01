import styles from './ChatTab.module.css'
import { ChatHistoryItem, Chat } from '../../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { LoadingButton } from '@mui/lab'
import { Menu, MenuItem, TextField } from '@mui/material'
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded'
import IconButton from '@mui/material/IconButton'
import React, { memo, useEffect, useRef, useState } from 'react'
import { ConfirmDialog } from '../../../../components/ConfirmDialog.tsx'
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded'
import EditRoundedIcon from '@mui/icons-material/EditRounded'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import { useDispatch, useSelector } from 'react-redux'
import { AppDispatch, RootState } from '../../../../state/store.ts'
import { setActiveChatIndex, createNewChat, fetchChatHistory } from '../../../../state/chat/chatHistorySlice.ts'
import chatApi from '../../../../services/api/chatApi.ts'
import { setChat } from '../../../../state/chat/chatSlice.ts'
import { SkeletonStack } from '../../../../components/loaders/SkeletonStack.tsx'
import { showErrorWithMessageAndError } from '../../../../components/snackbar/GlobalSnackbar.helpers.ts'

interface ChatHistoryProps {
  loadChat: (chatId: string) => Promise<void>,
  loadChatHistoryAndChatAndResult: (chatIndex: number) => Promise<void>
  databaseId: string,
}

/**
 * Chat history - list of previous chats.
 */
const ChatHistory = memo((
  {
    loadChat,
    loadChatHistoryAndChatAndResult,
    databaseId,
  }: ChatHistoryProps) => {

  const chatHistory: ChatHistoryItem[] = useSelector((state: RootState) => {
    return state.chatHistoryReducer.chatHistory
  })

  const chatHistoryLoading: boolean = useSelector((state: RootState) => {
    return state.chatHistoryReducer.loading
  })

  const createChatLoading: boolean = useSelector((state: RootState) => {
    return state.chatHistoryReducer.createNewChatLoading
  })

  const activeChatIndex: number = useSelector((state: RootState) => {
    return state.chatHistoryReducer.activeChatIndex
  })

  const dispatch: AppDispatch = useDispatch()

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

  const [
    deleteChatLoading,
    setDeleteChatLoading
  ] = useState<boolean>(false)

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

  async function confirmDeleteChat(): Promise<void> {
    if (chatToEdit) {
      setDeleteChatLoading(true)
      try {
        await chatApi.deleteChat(chatToEdit.id)
        // when last item is deleted
        let newActiveChatIndex = activeChatIndex
        if (activeChatIndex === chatHistory.length - 1) {
          newActiveChatIndex = activeChatIndex - 1
          dispatch(setActiveChatIndex(newActiveChatIndex))
        }
        await loadChatHistoryAndChatAndResult(newActiveChatIndex)
      } catch (error: unknown) {
        showErrorWithMessageAndError(dispatch, 'Failed to delete chat', error)
      } finally {
        setDeleteChatLoading(false)
      }
    }
  }

  function renameChatMenuClick(): void {
    closeMenu()
    if (chatToEdit) {
      setChatToRenameId(chatToEdit.id)
    }
  }

  function openChat(id: string, index: number): void {
    dispatch(setActiveChatIndex(index))
    void loadChat(id)
  }

  async function createChat(): Promise<void> {
    const result = await dispatch(createNewChat(databaseId))
    dispatch(setChat(result.payload as Chat))
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
    /* eslint-disable */
  }, [chatToRenameId])

  function renameChatOnBlur(event: React.FocusEvent<HTMLInputElement>): void {
    void reallyRenameChat(event.target.value)
  }

  function renameChatOnEnterPress(event: React.KeyboardEvent<HTMLDivElement>): void {
    if (event.key === 'Enter') {
      void reallyRenameChat(newName)
    }
  }

  async function reallyRenameChat(newName: string): Promise<void> {
    if (chatToRenameId && newName) {
      await chatApi.renameChat(chatToRenameId, newName)
      dispatch(fetchChatHistory(databaseId))
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
      deleteButtonLoading={deleteChatLoading}
    />

  return (
    <div className={styles.chatHistory}>
      {CreateNewChatButton}

      <div className={styles.chatHistoryList}>
        {chatHistoryLoading
          ? <SkeletonStack count={3} height={40}/>
          : <div>
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
})

export { ChatHistory }