'use client'

import { useSession, signOut } from 'next-auth/react'
import { useEffect, useRef, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { ExtendedSession } from '../../auth'

interface SessionManagementOptions {
  refreshThreshold?: number // Minutes before expiry to refresh (default: 5)
  maxInactivityTime?: number // Minutes of inactivity before warning (default: 30)
  warningDuration?: number // Minutes to show warning before auto logout (default: 5)
  enableActivityTracking?: boolean // Track user activity (default: true)
}

export function useSessionManagement(options: SessionManagementOptions = {}) {
  const {
    refreshThreshold = 5,
    maxInactivityTime = 30,
    warningDuration = 5,
    enableActivityTracking = true
  } = options

  const { data: session, status, update } = useSession()
  const router = useRouter()
  const extendedSession = session as ExtendedSession | null
  
  const lastActivityRef = useRef<Date>(new Date())
  const refreshTimerRef = useRef<NodeJS.Timeout>()
  const inactivityTimerRef = useRef<NodeJS.Timeout>()
  const warningTimerRef = useRef<NodeJS.Timeout>()
  const isRefreshingRef = useRef<boolean>(false)

  // Update last activity time
  const updateActivity = useCallback(() => {
    lastActivityRef.current = new Date()
    
    // Clear any existing inactivity timers
    if (inactivityTimerRef.current) {
      clearTimeout(inactivityTimerRef.current)
    }
    if (warningTimerRef.current) {
      clearTimeout(warningTimerRef.current)
    }

    // Set new inactivity timer
    if (enableActivityTracking && status === 'authenticated') {
      inactivityTimerRef.current = setTimeout(() => {
        showInactivityWarning()
      }, maxInactivityTime * 60 * 1000)
    }
  }, [enableActivityTracking, maxInactivityTime, status])

  // Show inactivity warning
  const showInactivityWarning = useCallback(() => {
    const shouldLogout = window.confirm(
      `${maxInactivityTime}분 동안 활동이 없었습니다. ${warningDuration}분 후 자동으로 로그아웃됩니다. 계속 사용하시겠습니까?`
    )

    if (shouldLogout) {
      updateActivity() // Reset activity timer
    } else {
      // Set warning timer for auto logout
      warningTimerRef.current = setTimeout(() => {
        handleAutoLogout('inactivity')
      }, warningDuration * 60 * 1000)
    }
  }, [maxInactivityTime, warningDuration, updateActivity])

  // Handle automatic logout
  const handleAutoLogout = useCallback(async (reason: 'inactivity' | 'expired') => {
    const message = reason === 'inactivity' 
      ? '비활성 상태로 인해 자동 로그아웃됩니다.'
      : '세션이 만료되어 자동 로그아웃됩니다.'
    
    alert(message)
    
    try {
      await signOut({ 
        callbackUrl: '/auth/signin?message=' + encodeURIComponent(message)
      })
    } catch (error) {
      console.error('Auto logout failed:', error)
      // Fallback: redirect manually
      window.location.href = '/auth/signin'
    }
  }, [])

  // Refresh access token
  const refreshAccessToken = useCallback(async () => {
    if (isRefreshingRef.current || !extendedSession?.refreshToken) {
      return false
    }

    isRefreshingRef.current = true

    try {
      const response = await fetch('/api/v1/sessions/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          refreshToken: extendedSession.refreshToken
        })
      })

      if (response.ok) {
        const data = await response.json()
        
        if (data.success) {
          // Update session with new access token
          await update({
            ...extendedSession,
            accessToken: data.data.accessToken,
            expires: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24 hours
          })
          
          console.log('Access token refreshed successfully')
          return true
        }
      }
      
      throw new Error('Token refresh failed')
      
    } catch (error) {
      console.error('Failed to refresh token:', error)
      handleAutoLogout('expired')
      return false
    } finally {
      isRefreshingRef.current = false
    }
  }, [extendedSession, update, handleAutoLogout])

  // Check if token needs refresh
  const checkTokenRefresh = useCallback(() => {
    if (!extendedSession?.expires) return

    const expiryTime = new Date(extendedSession.expires)
    const now = new Date()
    const minutesUntilExpiry = (expiryTime.getTime() - now.getTime()) / (1000 * 60)

    if (minutesUntilExpiry <= refreshThreshold && minutesUntilExpiry > 0) {
      refreshAccessToken()
    } else if (minutesUntilExpiry <= 0) {
      handleAutoLogout('expired')
    }
  }, [extendedSession, refreshThreshold, refreshAccessToken, handleAutoLogout])

  // Set up activity listeners
  useEffect(() => {
    if (!enableActivityTracking || status !== 'authenticated') return

    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
    
    const handleActivity = () => updateActivity()
    
    // Add event listeners
    events.forEach(event => {
      document.addEventListener(event, handleActivity, { passive: true })
    })

    // Initial activity update
    updateActivity()

    return () => {
      // Clean up event listeners
      events.forEach(event => {
        document.removeEventListener(event, handleActivity)
      })
    }
  }, [enableActivityTracking, status, updateActivity])

  // Set up token refresh timer
  useEffect(() => {
    if (status !== 'authenticated' || !extendedSession) return

    // Check immediately
    checkTokenRefresh()

    // Set up periodic check (every minute)
    refreshTimerRef.current = setInterval(checkTokenRefresh, 60 * 1000)

    return () => {
      if (refreshTimerRef.current) {
        clearInterval(refreshTimerRef.current)
      }
    }
  }, [status, extendedSession, checkTokenRefresh])

  // Clean up timers on unmount
  useEffect(() => {
    return () => {
      if (refreshTimerRef.current) clearInterval(refreshTimerRef.current)
      if (inactivityTimerRef.current) clearTimeout(inactivityTimerRef.current)
      if (warningTimerRef.current) clearTimeout(warningTimerRef.current)
    }
  }, [])

  return {
    session: extendedSession,
    status,
    isRefreshing: isRefreshingRef.current,
    lastActivity: lastActivityRef.current,
    refreshAccessToken,
    updateActivity
  }
}