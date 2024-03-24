import { Editor } from '@monaco-editor/react'
import * as monacoEditor from 'monaco-editor'
import React, { useRef } from 'react'

export function QueryEditor({ value, setValue }: {
  value: string,
  setValue: React.Dispatch<React.SetStateAction<string>>
}) {

  const editor = useRef<monacoEditor.editor.IStandaloneCodeEditor>()
  const monaco = useRef<typeof monacoEditor>()

  function handleEditorChange(value: string) {
    setValue(value)
  }

  function handleEditorDidMount(
    editorParam: monacoEditor.editor.IStandaloneCodeEditor,
    monacoParam: typeof monacoEditor): void {

    editor.current = editorParam
    monaco.current = monacoParam
  }

  const options = {
    inlineSuggest: true,
    fontSize: 16,
    fontFamily: 'monospace',
    lineHeight: 24,
    formatOnType: true,
    autoClosingBrackets: true,
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
      /></>
  )
}