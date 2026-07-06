import { deleteJson, getJson, postJson, putJson } from './client'
import type { Facility } from './types'

export function listFacilities(token: string): Promise<Facility[]> {
  return getJson<Facility[]>('/facilities', token)
}

export function createFacility(name: string, token: string): Promise<Facility> {
  return postJson<Facility>('/facilities', { name }, token)
}

export function updateFacility(id: number, name: string, token: string): Promise<Facility> {
  return putJson<Facility>(`/facilities/${id}`, { name }, token)
}

export function deleteFacility(id: number, token: string): Promise<void> {
  return deleteJson<void>(`/facilities/${id}`, token)
}
