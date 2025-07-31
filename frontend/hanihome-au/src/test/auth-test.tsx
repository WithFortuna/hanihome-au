'use client'

import { useAuth } from '../hooks/use-auth'
import { userApi, authApi } from '../lib/api/client'

export function AuthTest() {
  const { user, isAuthenticated, hasRole, status } = useAuth()

  const testApiCalls = async () => {
    try {
      console.log('Testing authenticated API calls...')
      
      // Test user profile API
      const profile = await userApi.getProfile()
      console.log('Profile:', profile)
      
      // Test user permissions
      const permissions = await userApi.getUserPermissions()
      console.log('Permissions:', permissions)
      
      // Test menu API
      const menu = await userApi.getMenu()
      console.log('Menu:', menu)
      
    } catch (error) {
      console.error('API test failed:', error)
    }
  }

  return (
    <div className="p-6 bg-white rounded-lg shadow">
      <h2 className="text-xl font-bold mb-4">Authentication Test</h2>
      
      <div className="space-y-4">
        <div>
          <strong>Status:</strong> {status}
        </div>
        
        <div>
          <strong>Authenticated:</strong> {isAuthenticated ? 'Yes' : 'No'}
        </div>
        
        {user && (
          <div>
            <strong>User:</strong>
            <pre className="bg-gray-100 p-2 rounded mt-2">
              {JSON.stringify(user, null, 2)}
            </pre>
          </div>
        )}
        
        <div>
          <strong>Role Checks:</strong>
          <ul className="list-disc list-inside mt-2">
            <li>Is ADMIN: {hasRole('ADMIN') ? 'Yes' : 'No'}</li>
            <li>Is TENANT: {hasRole('TENANT') ? 'Yes' : 'No'}</li>
            <li>Is LANDLORD: {hasRole('LANDLORD') ? 'Yes' : 'No'}</li>
            <li>Is AGENT: {hasRole('AGENT') ? 'Yes' : 'No'}</li>
          </ul>
        </div>
        
        <button
          onClick={testApiCalls}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          disabled={!isAuthenticated}
        >
          Test API Calls
        </button>
      </div>
    </div>
  )
}