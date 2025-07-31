import { getSession } from "next-auth/react"
import { ExtendedSession } from "../../../auth"

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

class ApiClient {
  private baseURL: string

  constructor() {
    this.baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"
  }

  private async getAuthHeaders(): Promise<HeadersInit> {
    const session = await getSession() as ExtendedSession | null
    
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    }

    if (session?.accessToken) {
      headers.Authorization = `Bearer ${session.accessToken}`
    }

    return headers
  }

  private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({
        success: false,
        message: `HTTP ${response.status}: ${response.statusText}`,
        data: null,
        timestamp: new Date().toISOString(),
      }))
      
      // Handle specific error types
      if (response.status === 401) {
        // Unauthorized - redirect to login
        if (typeof window !== 'undefined') {
          window.location.href = '/auth/signin?callbackUrl=' + encodeURIComponent(window.location.pathname)
        }
        throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.')
      }
      
      if (response.status === 403) {
        // Forbidden - show user-friendly message
        const message = errorData.message || '이 작업을 수행할 권한이 없습니다.'
        throw new Error(message)
      }
      
      if (response.status >= 500) {
        // Server error
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
      }
      
      throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`)
    }

    return response.json()
  }

  async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    const headers = await this.getAuthHeaders()
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "GET",
      headers,
    })

    return this.handleResponse<T>(response)
  }

  async post<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    const headers = await this.getAuthHeaders()
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "POST",
      headers,
      body: data ? JSON.stringify(data) : undefined,
    })

    return this.handleResponse<T>(response)
  }

  async put<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    const headers = await this.getAuthHeaders()
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "PUT",
      headers,
      body: data ? JSON.stringify(data) : undefined,
    })

    return this.handleResponse<T>(response)
  }

  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    const headers = await this.getAuthHeaders()
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "DELETE",
      headers,
    })

    return this.handleResponse<T>(response)
  }

  async patch<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    const headers = await this.getAuthHeaders()
    
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: "PATCH",
      headers,
      body: data ? JSON.stringify(data) : undefined,
    })

    return this.handleResponse<T>(response)
  }
}

export const apiClient = new ApiClient()

// Specific API methods
export const userApi = {
  getProfile: () => apiClient.get<any>("/api/v1/users/profile"),
  updateProfile: (data: any) => apiClient.put<any>("/api/v1/users/profile", data),
  getUserPermissions: () => apiClient.get<any>("/api/v1/menu/user-permissions"),
  getMenu: () => apiClient.get<any>("/api/v1/menu"),
  getAvailableFeatures: () => apiClient.get<any>("/api/v1/menu/features"),
}

export const authApi = {
  refreshToken: (refreshToken: string) => 
    apiClient.post<any>("/api/v1/auth/refresh", { refreshToken }),
  logout: () => apiClient.post<any>("/api/v1/auth/logout"),
  getUserInfo: () => apiClient.get<any>("/api/v1/auth/user"),
}

export const propertyApi = {
  getAll: () => apiClient.get<any>("/api/v1/properties"),
  getById: (id: string) => apiClient.get<any>(`/api/v1/properties/${id}`),
  create: (data: any) => apiClient.post<any>("/api/v1/properties", data),
  update: (id: string, data: any) => apiClient.put<any>(`/api/v1/properties/${id}`, data),
  delete: (id: string) => apiClient.delete<any>(`/api/v1/properties/${id}`),
  getMyProperties: () => apiClient.get<any>("/api/v1/properties/my-properties"),
}

// Protected API endpoints
export const protectedApi = {
  // General protected endpoints
  getProfile: () => apiClient.get<any>("/api/v1/protected/profile"),
  createResource: (data: any) => apiClient.post<any>("/api/v1/protected/resource", data),
  updateResource: (id: string, data: any) => apiClient.put<any>(`/api/v1/protected/resource/${id}`, data),
  deleteResource: (id: string) => apiClient.delete<any>(`/api/v1/protected/resource/${id}`),
  
  // Role-specific endpoints
  getTenantData: () => apiClient.get<any>("/api/v1/protected/tenant-only"),
  getLandlordData: () => apiClient.get<any>("/api/v1/protected/landlord-only"),
  getAgentAdminData: () => apiClient.get<any>("/api/v1/protected/agent-admin"),
  getAdminData: () => apiClient.get<any>("/api/v1/protected/admin-only"),
}

// Admin-only API endpoints
export const adminApi = {
  getAllUsers: () => apiClient.get<any>("/api/v1/admin/users"),
  updateUser: (id: string, data: any) => apiClient.put<any>(`/api/v1/admin/users/${id}`, data),
  deleteUser: (id: string) => apiClient.delete<any>(`/api/v1/admin/users/${id}`),
  getSystemStats: () => apiClient.get<any>("/api/v1/admin/stats"),
}

// Agent API endpoints
export const agentApi = {
  getAssignedProperties: () => apiClient.get<any>("/api/v1/agent/properties"),
  updatePropertyStatus: (id: string, status: string) => 
    apiClient.patch<any>(`/api/v1/agent/properties/${id}/status`, { status }),
  getClientList: () => apiClient.get<any>("/api/v1/agent/clients"),
}

// Landlord API endpoints
export const landlordApi = {
  getMyProperties: () => apiClient.get<any>("/api/v1/landlord/properties"),
  getTenantApplications: () => apiClient.get<any>("/api/v1/landlord/applications"),
  approveApplication: (id: string) => apiClient.post<any>(`/api/v1/landlord/applications/${id}/approve`),
  rejectApplication: (id: string) => apiClient.post<any>(`/api/v1/landlord/applications/${id}/reject`),
}

// Session management API endpoints
export const sessionApi = {
  getMySessions: () => apiClient.get<any>("/api/v1/sessions/my-sessions"),
  refreshToken: (refreshToken: string) => 
    apiClient.post<any>("/api/v1/sessions/refresh", { refreshToken }),
  invalidateSession: (sessionId: string) => 
    apiClient.delete<any>(`/api/v1/sessions/${sessionId}`),
  invalidateOtherSessions: (currentSessionId: string) => 
    apiClient.post<any>("/api/v1/sessions/invalidate-others", { currentSessionId }),
  getSessionStatistics: () => apiClient.get<any>("/api/v1/sessions/statistics"),
  manualCleanup: () => apiClient.post<any>("/api/v1/sessions/cleanup"),
}