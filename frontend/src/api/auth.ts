import { patchJson, postJson } from './client'
import type { AuthResponse, ChangePasswordPayload, LoginPayload, RegisterPayload } from './types'

export function register(payload: RegisterPayload): Promise<AuthResponse> {
  return postJson<AuthResponse>('/auth/register', payload)
}

export function login(payload: LoginPayload): Promise<AuthResponse> {
  return postJson<AuthResponse>('/auth/login', payload)
}

export function changePassword(payload: ChangePasswordPayload, token: string): Promise<void> {
  return patchJson<void>('/auth/change-password', payload, token)
}
