'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useAuth } from '../../hooks/use-auth'
import { Metadata } from 'next'

export default function UnauthorizedPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const { user, signOut } = useAuth()
  
  const requiredRoles = searchParams.get('requiredRoles')?.split(',') || []
  const userRole = searchParams.get('userRole') || user?.role
  const attemptedPath = searchParams.get('attemptedPath') || '알 수 없음'

  const handleGoBack = () => {
    router.back()
  }

  const handleGoToDashboard = () => {
    router.push('/dashboard')
  }

  const handleSignOut = () => {
    signOut()
  }

  const getRoleDisplayName = (role: string) => {
    const roleNames = {
      'ADMIN': '관리자',
      'AGENT': '에이전트',
      'LANDLORD': '집주인',
      'TENANT': '임차인'
    }
    return roleNames[role as keyof typeof roleNames] || role
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
          <svg
            className="h-6 w-6 text-red-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.726-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"
            />
          </svg>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          접근 권한이 없습니다
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          요청하신 페이지에 접근할 수 있는 권한이 없습니다.
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">접근 정보</h3>
              
              <div className="bg-gray-50 p-4 rounded-md space-y-3">
                <div>
                  <dt className="text-sm font-medium text-gray-500">시도한 경로:</dt>
                  <dd className="text-sm text-gray-900 font-mono bg-gray-100 px-2 py-1 rounded mt-1">
                    {attemptedPath}
                  </dd>
                </div>
                
                <div>
                  <dt className="text-sm font-medium text-gray-500">현재 권한:</dt>
                  <dd className="text-sm text-gray-900 mt-1">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      {userRole ? getRoleDisplayName(userRole) : '알 수 없음'}
                    </span>
                  </dd>
                </div>
                
                {requiredRoles.length > 0 && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">필요한 권한:</dt>
                    <dd className="text-sm text-gray-900 mt-1">
                      <div className="flex flex-wrap gap-1">
                        {requiredRoles.map((role, index) => (
                          <span
                            key={index}
                            className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800"
                          >
                            {getRoleDisplayName(role)}
                          </span>
                        ))}
                      </div>
                    </dd>
                  </div>
                )}
              </div>
            </div>

            <div className="space-y-3">
              <button
                onClick={handleGoBack}
                className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                이전 페이지로 돌아가기
              </button>
              
              <button
                onClick={handleGoToDashboard}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                대시보드로 이동
              </button>
            </div>

            {user && (
              <div className="pt-4 border-t border-gray-200">
                <div className="text-sm text-gray-500 mb-3">
                  다른 계정으로 로그인하시겠습니까?
                </div>
                <button
                  onClick={handleSignOut}
                  className="w-full flex justify-center py-2 px-4 border border-red-300 rounded-md shadow-sm text-sm font-medium text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                >
                  로그아웃
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-gray-50 text-gray-500">문의사항이 있으시면</span>
            </div>
          </div>

          <div className="mt-6 text-center">
            <a
              href="mailto:support@hanihome.com.au"
              className="text-sm text-blue-600 hover:text-blue-500"
            >
              support@hanihome.com.au로 연락주세요
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}