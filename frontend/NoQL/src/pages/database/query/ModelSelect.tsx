import { FormControl, MenuItem } from '@mui/material'
import { Select, SelectChangeEvent } from '@mui/material-next'
import { LlmModel } from '../../../types/Query.ts'
import React from 'react'
import styles from './Query.module.css'

interface ModelSelectProps {
  model: LlmModel,
  setModel: React.Dispatch<React.SetStateAction<LlmModel>>
}

export function ModelSelect({ model, setModel }: ModelSelectProps) {

  function selectModel(event: SelectChangeEvent<LlmModel>) {
    setModel(event.target.value as LlmModel)
  }

  function mapModelToLabel(model: string): string {
    switch (model) {
      case LlmModel.GPT_4o:
        return 'GPT 4o'
      case LlmModel.GPT_4:
        return 'GPT 4'
      case LlmModel.GPT_4_TURBO:
        return 'GPT 4 Turbo'
      case LlmModel.GPT_4_32K:
        return 'GPT 4 32k'
      case LlmModel.GPT_3_5_TURBO:
        return 'GPT 3.5 Turbo'
      case LlmModel.LLAMA3_70B:
        return 'Llama3 70B'
      case LlmModel.LLAMA3_13B_CHAT:
        return 'Llama3 13B Chat'
      default:
        return ''
    }
  }

  return (
    <div>
      <FormControl>
        <Select
          value={model}
          onChange={selectModel}
          size='small'
          className={styles.modelSelect}
        >
          {Object.keys(LlmModel)
            .filter((key: string) => isNaN(Number(key)))
            .map((model: string) => {
              return <MenuItem key={model} value={model}>{mapModelToLabel(model)}</MenuItem>
            })}
        </Select>
      </FormControl>
    </div>
  )
}