import { getJson, postJson } from './client'
import type { CreateProjectPayload, Project, ProjectTimeSummary } from './types'

export function listProjects(token: string): Promise<Project[]> {
  return getJson<Project[]>('/projects', token)
}

export function createProject(payload: CreateProjectPayload, token: string): Promise<Project> {
  return postJson<Project>('/projects', payload, token)
}

export function getProjectTimeSummary(id: number, token: string): Promise<ProjectTimeSummary> {
  return getJson<ProjectTimeSummary>(`/projects/${id}/summary`, token)
}
