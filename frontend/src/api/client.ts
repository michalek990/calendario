import { ApiError } from './types'

const API_BASE = '/api'

type Method = 'GET' | 'POST' | 'PATCH' | 'PUT'

async function request<TResponse>(method: Method, path: string, body?: unknown, token?: string): Promise<TResponse> {
  const headers: Record<string, string> = {}
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(await extractErrorMessage(response), response.status)
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return (await response.json()) as TResponse
}

export function getJson<TResponse>(path: string, token?: string): Promise<TResponse> {
  return request<TResponse>('GET', path, undefined, token)
}

export function postJson<TResponse>(path: string, body?: unknown, token?: string): Promise<TResponse> {
  return request<TResponse>('POST', path, body, token)
}

export function patchJson<TResponse>(path: string, body?: unknown, token?: string): Promise<TResponse> {
  return request<TResponse>('PATCH', path, body, token)
}

export function putJson<TResponse>(path: string, body?: unknown, token?: string): Promise<TResponse> {
  return request<TResponse>('PUT', path, body, token)
}

async function extractErrorMessage(response: Response): Promise<string> {
  try {
    const data = await response.json()
    if (typeof data.message === 'string') {
      return data.message
    }
  } catch {
    // odpowiedź nie jest JSON-em (np. błąd walidacji Springa ma inny kształt) — użyj fallbacku
  }
  return `Żądanie nie powiodło się (status ${response.status})`
}
