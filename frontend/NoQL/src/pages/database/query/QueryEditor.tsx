import React, { useRef } from 'react'
import { Editor } from '@monaco-editor/react'
import * as monacoEditor from 'monaco-editor'
import { editor } from 'monaco-editor'

interface Props {
  value: string,
  setValue: React.Dispatch<React.SetStateAction<string>>
}

export function QueryEditor({ value, setValue }: Props) {

  const editor = useRef<monacoEditor.editor.IStandaloneCodeEditor>()
  const monaco = useRef<typeof monacoEditor>()

  function handleEditorChange(value: string | undefined, _ev: editor.IModelContentChangedEvent): void {
    setValue(value || '')
  }

  function handleEditorDidMount(
    editorParam: monacoEditor.editor.IStandaloneCodeEditor,
    monacoParam: typeof monacoEditor): void {

    editor.current = editorParam
    monaco.current = monacoParam
  }

  const options: editor.IStandaloneEditorConstructionOptions = {
    inlineSuggest: { enabled: true },
    fontSize: 16,
    fontFamily: 'monospace',
    lineHeight: 24,
    formatOnType: true,
    autoClosingBrackets: 'always',
    minimap: { enabled: false },
    padding: { top: 20 }
  }

  return (
    <>
      <Editor
        height="200px"
        language="sql"
        theme="vs-dark"
        value={value}
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        options={options}
      />
    </>
  )
}