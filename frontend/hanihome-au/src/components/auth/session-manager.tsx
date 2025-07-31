'use client'

import { useEffect } from 'react'
import { useSessionManagement } from '../../hooks/use-session-management'

interface SessionManagerProps {
  children: React.ReactNode
  refreshThreshold?: number
  maxInactivityTime?: number
  warningDuration?: number
  enableActivityTracking?: boolean
}

export function SessionManager({ 
  children,
  refreshThreshold = 5,
  maxInactivityTime = 30,
  warningDuration = 5,
  enableActivityTracking = true
}: SessionManagerProps) {
  const { session, status } = useSessionManagement({
    refreshThreshold,
    maxInactivityTime,
    warningDuration,
    enableActivityTracking
  })

  // Add visual indicator for session status (optional)
  useEffect(() => {
    if (status === 'authenticated') {
      // Remove any existing session warning styles
      document.body.classList.remove('session-warning')
    } else if (status === 'unauthenticated') {
      // Add session warning styles if needed
      document.body.classList.add('session-warning')
    }

    return () => {
      document.body.classList.remove('session-warning')
    }
  }, [status])

  return <>{children}</>
}

// Optional: Session status indicator component
export function SessionStatusIndicator() {
  const { session, status, isRefreshing, lastActivity } = useSessionManagement()

  if (status !== 'authenticated') return null

  const getStatusColor = () => {
    if (isRefreshing) return 'bg-yellow-500'
    
    const minutesSinceActivity = (Date.now() - lastActivity.getTime()) / (1000 * 60)
    if (minutesSinceActivity > 25) return 'bg-red-500'
    if (minutesSinceActivity > 15) return 'bg-orange-500'
    return 'bg-green-500'
  }

  const getStatusText = () => {
    if (isRefreshing) return '토큰 갱신 중...'
    
    const minutesSinceActivity = Math.floor((Date.now() - lastActivity.getTime()) / (1000 * 60))
    if (minutesSinceActivity > 25) return '세션 만료 임박'
    if (minutesSinceActivity > 15) return '비활성 상태'
    return '활성'
  }

  return (
    <div className="fixed bottom-4 right-4 z-50">
      <div className="flex items-center space-x-2 bg-white rounded-lg shadow-lg px-3 py-2 border">
        <div className={`w-2 h-2 rounded-full ${getStatusColor()}`}></div>
        <span className="text-xs text-gray-600">{getStatusText()}</span>
      </div>
    </div>
  )
}