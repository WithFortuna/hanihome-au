"use client"

import { useSession } from "next-auth/react"
import { ExtendedSession } from "../../auth"

export function useAuth() {
  const { data: session, status } = useSession()
  const extendedSession = session as ExtendedSession | null

  return {
    user: extendedSession?.user,
    isAuthenticated: status === "authenticated",
    isLoading: status === "loading",
    accessToken: extendedSession?.accessToken,
    refreshToken: extendedSession?.refreshToken,
    hasRole: (role: string) => extendedSession?.user?.role === role,
    hasAnyRole: (roles: string[]) => roles.includes(extendedSession?.user?.role || ""),
    isAdmin: () => extendedSession?.user?.role === "ADMIN",
    isAgent: () => extendedSession?.user?.role === "AGENT",
    isLandlord: () => extendedSession?.user?.role === "LANDLORD",
    isTenant: () => extendedSession?.user?.role === "TENANT",
  }
}