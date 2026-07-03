import { postJson } from './client'
import type { AuthResponse, LoginPayload, RegisterPayload } from './types'

export function register(payload: RegisterPayload): Promise<AuthResponse> {
  return postJson<AuthResponse>('/auth/register', payload)
}

export function login(payload: LoginPayload): Promise<AuthResponse> {
  return postJson<AuthResponse>('/auth/login', payload)
}
