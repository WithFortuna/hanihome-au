'use client'

import { ReactNode } from 'react'
import { useRole } from './with-auth'

interface RoleGuardProps {
  children: ReactNode
  allowedRoles?: string[]
  fallback?: ReactNode
  requireAll?: boolean // If true, user must have ALL roles, if false (default), user needs ANY role
}

export function RoleGuard({ 
  children, 
  allowedRoles = [], 
  fallback = null,
  requireAll = false 
}: RoleGuardProps) {
  const { userRole, hasAnyRole, isAuthenticated } = useRole()
  
  // If not authenticated, don't show anything
  if (!isAuthenticated) {
    return <>{fallback}</>
  }
  
  // If no roles specified, show to any authenticated user
  if (allowedRoles.length === 0) {
    return <>{children}</>
  }
  
  // Check role permissions
  const hasPermission = requireAll 
    ? allowedRoles.every(role => userRole === role) // Must have all roles (edge case)
    : hasAnyRole(allowedRoles) // Must have at least one role
  
  if (hasPermission) {
    return <>{children}</>
  }
  
  return <>{fallback}</>
}

// Convenience components for specific roles
export function AdminOnly({ children, fallback }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <RoleGuard allowedRoles={['ADMIN']} fallback={fallback}>
      {children}
    </RoleGuard>
  )
}

export function AgentOnly({ children, fallback }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <RoleGuard allowedRoles={['AGENT', 'ADMIN']} fallback={fallback}>
      {children}
    </RoleGuard>
  )
}

export function LandlordOnly({ children, fallback }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <RoleGuard allowedRoles={['LANDLORD', 'ADMIN']} fallback={fallback}>
      {children}
    </RoleGuard>
  )
}

export function TenantOnly({ children, fallback }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <RoleGuard allowedRoles={['TENANT', 'ADMIN']} fallback={fallback}>
      {children}
    </RoleGuard>
  )
}

// Component for showing content to multiple roles
export function ForRoles({ 
  roles, 
  children, 
  fallback 
}: { 
  roles: string[]
  children: ReactNode
  fallback?: ReactNode 
}) {
  return (
    <RoleGuard allowedRoles={roles} fallback={fallback}>
      {children}
    </RoleGuard>
  )
}

// Component for showing different content based on role
export function RoleSwitch({ 
  admin, 
  agent, 
  landlord, 
  tenant, 
  fallback 
}: {
  admin?: ReactNode
  agent?: ReactNode
  landlord?: ReactNode
  tenant?: ReactNode
  fallback?: ReactNode
}) {
  const { userRole } = useRole()
  
  switch (userRole) {
    case 'ADMIN':
      return <>{admin || fallback}</>
    case 'AGENT':
      return <>{agent || fallback}</>
    case 'LANDLORD':
      return <>{landlord || fallback}</>
    case 'TENANT':
      return <>{tenant || fallback}</>
    default:
      return <>{fallback}</>
  }
}

// Hook for conditional logic in components
export function usePermissions() {
  const { userRole, hasRole, hasAnyRole, isAuthenticated } = useRole()
  
  return {
    userRole,
    isAuthenticated,
    isAdmin: hasRole('ADMIN'),
    isAgent: hasRole('AGENT') || hasRole('ADMIN'),
    isLandlord: hasRole('LANDLORD') || hasRole('ADMIN'),
    isTenant: hasRole('TENANT') || hasRole('ADMIN'),
    canManageProperties: hasAnyRole(['LANDLORD', 'AGENT', 'ADMIN']),
    canManageUsers: hasRole('ADMIN'),
    canViewReports: hasAnyRole(['AGENT', 'ADMIN']),
    canCreateListings: hasAnyRole(['LANDLORD', 'AGENT', 'ADMIN']),
    canApplyToProperties: hasRole('TENANT'),
    hasRole,
    hasAnyRole,
  }
}