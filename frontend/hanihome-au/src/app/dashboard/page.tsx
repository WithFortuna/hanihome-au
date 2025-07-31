'use client'

import { useState, useEffect } from "react"
import { UserProfile } from "../../components/auth/user-profile"
import { RoleGuard, AdminOnly, AgentOnly, LandlordOnly, TenantOnly, RoleSwitch, usePermissions } from "../../components/auth/role-guard"
import { protectedApi, adminApi, agentApi, landlordApi } from "../../lib/api/client"

export default function DashboardPage() {
  const [loading, setLoading] = useState(false)
  const [apiResults, setApiResults] = useState<Record<string, any>>({})
  const [error, setError] = useState<string | null>(null)
  const permissions = usePermissions()

  const testRoleBasedAPIs = async () => {
    setLoading(true)
    setError(null)
    const results: Record<string, any> = {}

    try {
      // Test general protected endpoint
      try {
        const profile = await protectedApi.getProfile()
        results.profile = profile
      } catch (err) {
        results.profile = { error: (err as Error).message }
      }

      // Test role-specific endpoints based on user role
      if (permissions.isTenant) {
        try {
          const tenantData = await protectedApi.getTenantData()
          results.tenantData = tenantData
        } catch (err) {
          results.tenantData = { error: (err as Error).message }
        }
      }

      if (permissions.isLandlord) {
        try {
          const landlordData = await protectedApi.getLandlordData()
          results.landlordData = landlordData
        } catch (err) {
          results.landlordData = { error: (err as Error).message }
        }
      }

      if (permissions.isAgent) {
        try {
          const agentData = await protectedApi.getAgentAdminData()
          results.agentData = agentData
        } catch (err) {
          results.agentData = { error: (err as Error).message }
        }
      }

      if (permissions.isAdmin) {
        try {
          const adminData = await protectedApi.getAdminData()
          results.adminData = adminData
        } catch (err) {
          results.adminData = { error: (err as Error).message }
        }
      }

      setApiResults(results)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="max-w-6xl mx-auto">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
              <RoleSwitch
                admin={<p className="text-gray-600">관리자 대시보드에 오신 것을 환영합니다</p>}
                agent={<p className="text-gray-600">에이전트 대시보드에 오신 것을 환영합니다</p>}
                landlord={<p className="text-gray-600">집주인 대시보드에 오신 것을 환영합니다</p>}
                tenant={<p className="text-gray-600">임차인 대시보드에 오신 것을 환영합니다</p>}
                fallback={<p className="text-gray-600">HaniHome AU 대시보드에 오신 것을 환영합니다</p>}
              />
            </div>
            <div className="text-sm text-gray-500">
              권한: <span className="font-medium text-blue-600">{permissions.userRole}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div className="max-w-6xl mx-auto p-4 space-y-6">
        {/* User Profile Section */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">사용자 정보</h2>
          <UserProfile />
        </div>

        {/* Role-based Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          
          {/* Admin Only Card */}
          <AdminOnly>
            <div className="bg-red-50 border border-red-200 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-red-800 mb-2">관리자 전용</h3>
              <p className="text-red-600 text-sm mb-4">시스템 관리 및 사용자 관리</p>
              <button className="bg-red-600 text-white px-4 py-2 rounded text-sm hover:bg-red-700">
                사용자 관리
              </button>
            </div>
          </AdminOnly>

          {/* Agent Only Card */}
          <AgentOnly>
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-blue-800 mb-2">에이전트 전용</h3>
              <p className="text-blue-600 text-sm mb-4">부동산 중개 및 고객 관리</p>
              <button className="bg-blue-600 text-white px-4 py-2 rounded text-sm hover:bg-blue-700">
                고객 관리
              </button>
            </div>
          </AgentOnly>

          {/* Landlord Only Card */}
          <LandlordOnly>
            <div className="bg-green-50 border border-green-200 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-green-800 mb-2">집주인 전용</h3>
              <p className="text-green-600 text-sm mb-4">부동산 관리 및 임차 신청 검토</p>
              <button className="bg-green-600 text-white px-4 py-2 rounded text-sm hover:bg-green-700">
                부동산 관리
              </button>
            </div>
          </LandlordOnly>

          {/* Tenant Only Card */}
          <TenantOnly>
            <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-purple-800 mb-2">임차인 전용</h3>
              <p className="text-purple-600 text-sm mb-4">부동산 검색 및 임차 신청</p>
              <button className="bg-purple-600 text-white px-4 py-2 rounded text-sm hover:bg-purple-700">
                부동산 검색
              </button>
            </div>
          </TenantOnly>

        </div>

        {/* Permissions Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">권한 요약</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div className={`p-3 rounded ${permissions.canManageProperties ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500'}`}>
              <div className="font-medium">부동산 관리</div>
              <div>{permissions.canManageProperties ? '가능' : '불가'}</div>
            </div>
            <div className={`p-3 rounded ${permissions.canManageUsers ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500'}`}>
              <div className="font-medium">사용자 관리</div>
              <div>{permissions.canManageUsers ? '가능' : '불가'}</div>
            </div>
            <div className={`p-3 rounded ${permissions.canViewReports ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500'}`}>
              <div className="font-medium">보고서 조회</div>
              <div>{permissions.canViewReports ? '가능' : '불가'}</div>
            </div>
            <div className={`p-3 rounded ${permissions.canCreateListings ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500'}`}>
              <div className="font-medium">매물 등록</div>
              <div>{permissions.canCreateListings ? '가능' : '불가'}</div>
            </div>
          </div>
        </div>

        {/* API Testing Section */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">보호된 API 테스트</h2>
            <button
              onClick={testRoleBasedAPIs}
              disabled={loading}
              className="bg-blue-600 text-white px-4 py-2 rounded text-sm hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? '테스트 중...' : 'API 테스트'}
            </button>
          </div>
          
          {error && (
            <div className="bg-red-50 border border-red-200 rounded p-3 mb-4">
              <p className="text-red-800 text-sm">{error}</p>
            </div>
          )}

          {Object.keys(apiResults).length > 0 && (
            <div className="space-y-4">
              {Object.entries(apiResults).map(([key, result]) => (
                <div key={key} className="border rounded p-3">
                  <h4 className="font-medium text-sm text-gray-700 mb-2">{key}</h4>
                  <pre className="text-xs bg-gray-50 p-2 rounded overflow-auto">
                    {JSON.stringify(result, null, 2)}
                  </pre>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Role-specific Quick Actions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">빠른 작업</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            
            <RoleGuard allowedRoles={['TENANT', 'ADMIN']}>
              <button className="w-full p-3 text-left border rounded hover:bg-gray-50">
                <div className="font-medium text-sm">임차 신청</div>
                <div className="text-xs text-gray-500">원하는 부동산에 신청하기</div>
              </button>
            </RoleGuard>

            <RoleGuard allowedRoles={['LANDLORD', 'AGENT', 'ADMIN']}>
              <button className="w-full p-3 text-left border rounded hover:bg-gray-50">
                <div className="font-medium text-sm">매물 등록</div>
                <div className="text-xs text-gray-500">새로운 부동산 등록하기</div>
              </button>
            </RoleGuard>

            <RoleGuard allowedRoles={['AGENT', 'ADMIN']}>
              <button className="w-full p-3 text-left border rounded hover:bg-gray-50">
                <div className="font-medium text-sm">고객 관리</div>
                <div className="text-xs text-gray-500">고객 정보 및 문의 관리</div>
              </button>
            </RoleGuard>

            <RoleGuard allowedRoles={['ADMIN']}>
              <button className="w-full p-3 text-left border rounded hover:bg-gray-50">
                <div className="font-medium text-sm">시스템 관리</div>
                <div className="text-xs text-gray-500">사용자 및 시스템 설정</div>
              </button>
            </RoleGuard>

          </div>
        </div>
      </div>
    </div>
  )
}