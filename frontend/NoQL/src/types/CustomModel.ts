interface CustomModel {
  id: string
  name: string,
  host: string
  port: number
}

interface CreateCustomModelRequest {
  name: string
  host: string
  port: number
  userId: string
}

interface UpdateCustomModelRequest {
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
  CreateCustomModelRequest,
  UpdateCustomModelRequest,
  ModelOption,
}