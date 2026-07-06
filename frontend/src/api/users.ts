import { getJson, patchJson } from './client'
import type { UpdateOrganizationPayload, UpdatePersonalInfoPayload, UpdateRolePayload, UserProfile } from './types'

export function getMyProfile(token: string): Promise<UserProfile> {
  return getJson<UserProfile>('/users/me/profile', token)
}

export function updateMyPersonalInfo(payload: UpdatePersonalInfoPayload, token: string): Promise<UserProfile> {
  return patchJson<UserProfile>('/users/me/personal-info', payload, token)
}

export function updateUserOrganization(
  userId: number,
  payload: UpdateOrganizationPayload,
  token: string,
): Promise<UserProfile> {
  return patchJson<UserProfile>(`/users/${userId}/profile`, payload, token)
}

export function listAllUsers(token: string): Promise<UserProfile[]> {
  return getJson<UserProfile[]>('/users', token)
}

export function updateUserRole(userId: number, payload: UpdateRolePayload, token: string): Promise<UserProfile> {
  return patchJson<UserProfile>(`/users/${userId}/role`, payload, token)
}
