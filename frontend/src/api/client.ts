import { ApiError } from './types'

const API_BASE = '/api'

export async function postJson<TResponse>(path: string, body: unknown, token?: string): Promise<TResponse> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw new ApiError(await extractErrorMessage(response), response.status)
  }

  return (await response.json()) as TResponse
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
