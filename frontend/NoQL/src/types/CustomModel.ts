interface CustomModel {
  id: string
  name: string,
  host: string
  port: number
}

interface CreateUpdateCustomModelRequest {
  name: string
  host: string
  port: number
}

interface ModelOption {
  label: string
  value: string
}

export type {
  CustomModel,
  CreateUpdateCustomModelRequest,
  ModelOption,
}