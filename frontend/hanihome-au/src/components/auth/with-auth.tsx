'use client'

import { useSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useEffect, ComponentType } from 'react'
import { ExtendedSession } from '../../../auth'

export interface WithAuthOptions {
  requiredRoles?: string[]
  redirectTo?: string
  loadingComponent?: ComponentType
  unauthorizedComponent?: ComponentType<{ requiredRoles: string[]; userRole?: string }>
}

export function withAuth<P extends {}>(
  WrappedComponent: ComponentType<P>,
  options: WithAuthOptions = {}
) {
  const {
    requiredRoles = [],
    redirectTo = '/auth/signin',
    loadingComponent: LoadingComponent,
    unauthorizedComponent: UnauthorizedComponent
  } = options

  return function WithAuthComponent(props: P) {
    const { data: session, status } = useSession()
    const router = useRouter()
    const extendedSession = session as ExtendedSession | null

    useEffect(() => {
      if (status === 'loading') return // Still loading

      if (status === 'unauthenticated') {
        router.push(redirectTo)
        return
      }

      if (requiredRoles.length > 0 && extendedSession?.user?.role) {
        const userRole = extendedSession.user.role
        const hasRequiredRole = requiredRoles.includes(userRole)
        
        if (!hasRequiredRole && UnauthorizedComponent) {
          // Will show unauthorized component instead of redirecting
          return
        } else if (!hasRequiredRole) {
          router.push('/unauthorized')
          return
        }
      }
    }, [status, extendedSession, router])

    // Loading state
    if (status === 'loading') {
      if (LoadingComponent) {
        return <LoadingComponent />
      }
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-2 text-gray-600">인증 정보를 확인하는 중...</span>
        </div>
      )
    }

    // Not authenticated
    if (status === 'unauthenticated') {
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <h2 className="text-xl font-semibold text-gray-900 mb-2">로그인이 필요합니다</h2>
            <p className="text-gray-600">잠시 후 로그인 페이지로 이동합니다...</p>
          </div>
        </div>
      )
    }

    // Check role authorization
    if (requiredRoles.length > 0 && extendedSession?.user?.role) {
      const userRole = extendedSession.user.role
      const hasRequiredRole = requiredRoles.includes(userRole)
      
      if (!hasRequiredRole && UnauthorizedComponent) {
        return <UnauthorizedComponent requiredRoles={requiredRoles} userRole={userRole} />
      }
      
      if (!hasRequiredRole) {
        return (
          <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
              <h2 className="text-xl font-semibold text-red-600 mb-2">접근 권한이 없습니다</h2>
              <p className="text-gray-600 mb-4">
                이 페이지에 접근하려면 다음 권한이 필요합니다: {requiredRoles.join(', ')}
              </p>
              <p className="text-sm text-gray-500">
                현재 권한: {userRole}
              </p>
              <button
                onClick={() => router.back()}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                뒤로 가기
              </button>
            </div>
          </div>
        )
      }
    }

    // User is authenticated and authorized
    return <WrappedComponent {...props} />
  }
}

// Default unauthorized component
export function DefaultUnauthorized({ requiredRoles, userRole }: { 
  requiredRoles: string[] 
  userRole?: string 
}) {
  const router = useRouter()
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-6 text-center">
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
          <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.726-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">접근 권한이 없습니다</h3>
        <p className="text-sm text-gray-500 mb-4">
          이 페이지에는 다음 권한이 필요합니다:
        </p>
        <div className="mb-4">
          {requiredRoles.map((role, index) => (
            <span
              key={role}
              className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded mr-1 mb-1"
            >
              {role}
            </span>
          ))}
        </div>
        {userRole && (
          <p className="text-sm text-gray-500 mb-4">
            현재 권한: <span className="font-medium">{userRole}</span>
          </p>
        )}
        <div className="space-y-2">
          <button
            onClick={() => router.back()}
            className="w-full px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
          >
            뒤로 가기
          </button>
          <button
            onClick={() => router.push('/dashboard')}
            className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            대시보드로 이동
          </button>
        </div>
      </div>
    </div>
  )
}

// Helper hook for conditional rendering based on roles
export function useRole() {
  const { data: session } = useSession()
  const extendedSession = session as ExtendedSession | null
  
  return {
    userRole: extendedSession?.user?.role,
    hasRole: (role: string) => extendedSession?.user?.role === role,
    hasAnyRole: (roles: string[]) => roles.includes(extendedSession?.user?.role || ''),
    isAuthenticated: !!extendedSession?.user
  }
}