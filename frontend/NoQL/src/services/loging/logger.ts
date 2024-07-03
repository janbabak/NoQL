export interface LogFunction {
  (message?: any, ...optionalParams: any[]): void
}

export interface Logger {
  info: LogFunction
  warn: LogFunction
  error: LogFunction
}

export type LogLevel = 'info' | 'warn' | 'error'

const NO_OP: LogFunction = (_message?: any, ..._optionalParams: any[]): void => {}

export class ConsoleLogger implements Logger {
  readonly info: LogFunction
  readonly warn: LogFunction
  readonly error: LogFunction

  constructor(options?: { level?: LogLevel }) {

    const { level } = options || {}

    this.error = console.error.bind(console)

    if (level === 'error') {
      this.info = NO_OP
      this.warn = NO_OP

      return
    }

    this.warn = console.warn.bind(console)

    if (level === 'warn') {
      this.info = NO_OP

      return;
    }

    this.info = console.log.bind(console)
  }
}

export const log = new ConsoleLogger({
  level: import.meta.env.MODE === 'development'
    ? 'info'
    : 'error'
})