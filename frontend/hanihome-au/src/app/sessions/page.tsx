'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '../../hooks/use-auth'
import { apiClient } from '../../lib/api/client'

interface SessionInfo {
  sessionId: string
  userId: number
  email: string
  role: string
  deviceInfo: string
  ipAddress: string
  createdAt: string
  lastAccessedAt: string
  active: boolean
  deviceType: string
  browser: string
  minutesSinceLastAccess: number
}

export default function SessionsPage() {
  const { user, isAuthenticated } = useAuth()
  const [sessions, setSessions] = useState<SessionInfo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionLoading, setActionLoading] = useState<string | null>(null)

  const fetchSessions = async () => {
    try {
      setLoading(true)
      const response = await apiClient.get<SessionInfo[]>('/api/v1/sessions/my-sessions')
      
      if (response.success) {
        setSessions(response.data)
      } else {
        setError(response.message)
      }
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  const invalidateSession = async (sessionId: string) => {
    try {
      setActionLoading(sessionId)
      const response = await apiClient.delete(`/api/v1/sessions/${sessionId}`)
      
      if (response.success) {
        // Remove the session from the list
        setSessions(sessions.filter(s => s.sessionId !== sessionId))
      } else {
        setError(response.message)
      }
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setActionLoading(null)
    }
  }

  const invalidateOtherSessions = async () => {
    const currentSession = sessions.find(s => s.minutesSinceLastAccess === 0)
    if (!currentSession) {
      setError('현재 세션을 찾을 수 없습니다.')
      return
    }

    const confirmed = window.confirm(
      '다른 모든 디바이스에서 로그아웃됩니다. 계속하시겠습니까?'
    )

    if (!confirmed) return

    try {
      setActionLoading('all-others')
      const response = await apiClient.post('/api/v1/sessions/invalidate-others', {
        currentSessionId: currentSession.sessionId
      })
      
      if (response.success) {
        // Keep only the current session
        setSessions(sessions.filter(s => s.sessionId === currentSession.sessionId))
      } else {
        setError(response.message)
      }
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setActionLoading(null)
    }
  }

  useEffect(() => {
    if (isAuthenticated) {
      fetchSessions()
    }
  }, [isAuthenticated])

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-gray-900 mb-2">로그인이 필요합니다</h2>
          <p className="text-gray-600">세션 관리를 위해 먼저 로그인해주세요.</p>
        </div>
      </div>
    )
  }

  const getDeviceIcon = (deviceType: string) => {
    switch (deviceType.toLowerCase()) {
      case 'mobile':
        return '📱'
      case 'tablet':
        return '📟'
      case 'desktop':
      default:
        return '💻'
    }
  }

  const getStatusColor = (session: SessionInfo) => {
    if (session.minutesSinceLastAccess === 0) return 'bg-green-100 text-green-800'
    if (session.minutesSinceLastAccess < 60) return 'bg-blue-100 text-blue-800'
    if (session.minutesSinceLastAccess < 1440) return 'bg-yellow-100 text-yellow-800'
    return 'bg-gray-100 text-gray-800'
  }

  const formatLastAccess = (minutes: number) => {
    if (minutes === 0) return '현재 세션'
    if (minutes < 60) return `${minutes}분 전`
    if (minutes < 1440) return `${Math.floor(minutes / 60)}시간 전`
    return `${Math.floor(minutes / 1440)}일 전`
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="max-w-6xl mx-auto">
          <h1 className="text-2xl font-bold text-gray-900">세션 관리</h1>
          <p className="text-gray-600">로그인된 디바이스와 세션을 관리할 수 있습니다</p>
        </div>
      </div>
      
      <div className="max-w-6xl mx-auto p-4">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-800">{error}</p>
              </div>
              <div className="ml-auto pl-3">
                <button
                  onClick={() => setError(null)}
                  className="text-red-400 hover:text-red-600"
                >
                  <span className="sr-only">닫기</span>
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-lg font-semibold text-gray-900">활성 세션</h2>
                <p className="text-sm text-gray-600">현재 로그인된 디바이스 목록</p>
              </div>
              <div className="space-x-3">
                <button
                  onClick={fetchSessions}
                  disabled={loading}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
                >
                  {loading ? '새로고침 중...' : '새로고침'}
                </button>
                {sessions.length > 1 && (
                  <button
                    onClick={invalidateOtherSessions}
                    disabled={actionLoading === 'all-others'}
                    className="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 disabled:opacity-50"
                  >
                    {actionLoading === 'all-others' ? '처리 중...' : '다른 디바이스 로그아웃'}
                  </button>
                )}
              </div>
            </div>
          </div>

          <div className="divide-y divide-gray-200">
            {loading ? (
              <div className="px-6 py-12 text-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-2 text-sm text-gray-500">세션 정보를 불러오는 중...</p>
              </div>
            ) : sessions.length === 0 ? (
              <div className="px-6 py-12 text-center">
                <p className="text-gray-500">활성 세션이 없습니다.</p>
              </div>
            ) : (
              sessions.map((session) => (
                <div key={session.sessionId} className="px-6 py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="text-2xl">
                        {getDeviceIcon(session.deviceType)}
                      </div>
                      <div>
                        <div className="flex items-center space-x-2">
                          <h3 className="text-sm font-medium text-gray-900">
                            {session.browser} on {session.deviceType}
                          </h3>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(session)}`}>
                            {formatLastAccess(session.minutesSinceLastAccess)}
                          </span>
                        </div>
                        <div className="mt-1 space-y-1">
                          <p className="text-xs text-gray-500">
                            IP 주소: {session.ipAddress}
                          </p>
                          <p className="text-xs text-gray-500">
                            생성일: {new Date(session.createdAt).toLocaleString('ko-KR')}
                          </p>
                          <p className="text-xs text-gray-500">
                            최근 접속: {new Date(session.lastAccessedAt).toLocaleString('ko-KR')}
                          </p>
                        </div>
                      </div>
                    </div>
                    
                    {session.minutesSinceLastAccess !== 0 && (
                      <button
                        onClick={() => invalidateSession(session.sessionId)}
                        disabled={actionLoading === session.sessionId}
                        className="px-3 py-1 text-xs font-medium text-red-700 bg-red-50 border border-red-200 rounded hover:bg-red-100 disabled:opacity-50"
                      >
                        {actionLoading === session.sessionId ? '처리 중...' : '로그아웃'}
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Security Tips */}
        <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 className="text-sm font-medium text-blue-800 mb-2">보안 팁</h3>
          <ul className="text-xs text-blue-700 space-y-1">
            <li>• 사용하지 않는 디바이스는 정기적으로 로그아웃해주세요</li>
            <li>• 의심스러운 접속 기록이 있다면 즉시 해당 세션을 종료하고 비밀번호를 변경하세요</li>
            <li>• 공용 컴퓨터에서 사용 후에는 반드시 로그아웃해주세요</li>
          </ul>
        </div>
      </div>
    </div>
  )
}