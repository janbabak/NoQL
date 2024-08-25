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

export type {
  CustomModel,
  CreateUpdateCustomModelRequest
}