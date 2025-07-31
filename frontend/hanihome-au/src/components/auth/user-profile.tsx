"use client"

import { useAuth } from "../../hooks/use-auth"
import { Button } from "../ui/button"
import { User, Mail, Shield, Calendar } from "lucide-react"
import { signOut } from "next-auth/react"

export function UserProfile() {
  const { user, isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
      </div>
    )
  }

  if (!isAuthenticated || !user) {
    return (
      <div className="text-center p-8">
        <p className="text-gray-600">Please sign in to view your profile.</p>
      </div>
    )
  }

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "bg-red-100 text-red-800 border-red-200"
      case "AGENT":
        return "bg-blue-100 text-blue-800 border-blue-200"
      case "LANDLORD":
        return "bg-green-100 text-green-800 border-green-200"
      case "TENANT":
        return "bg-gray-100 text-gray-800 border-gray-200"
      default:
        return "bg-gray-100 text-gray-800 border-gray-200"
    }
  }

  const getRoleDescription = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "System Administrator with full access to all features"
      case "AGENT":
        return "Real Estate Agent with property and client management capabilities"
      case "LANDLORD":
        return "Property Owner with tenant and property management access"
      case "TENANT":
        return "Property Renter with search and application capabilities"
      default:
        return "Standard user account"
    }
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-md border p-8">
        <div className="flex items-center space-x-4 mb-6">
          <div className="h-16 w-16 bg-gray-200 rounded-full flex items-center justify-center">
            {user.image ? (
              <img
                src={user.image}
                alt={user.name || "User"}
                className="h-16 w-16 rounded-full object-cover"
              />
            ) : (
              <User className="h-8 w-8 text-gray-600" />
            )}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {user.name || "Anonymous User"}
            </h1>
            <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getRoleBadgeColor(user.role)}`}>
              <Shield className="h-3 w-3 mr-1" />
              {user.role}
            </div>
          </div>
        </div>

        <div className="space-y-4 mb-6">
          <div className="flex items-center space-x-3">
            <Mail className="h-5 w-5 text-gray-500" />
            <div>
              <p className="text-sm text-gray-500">Email</p>
              <p className="text-gray-900">{user.email}</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <User className="h-5 w-5 text-gray-500" />
            <div>
              <p className="text-sm text-gray-500">User ID</p>
              <p className="text-gray-900 font-mono text-sm">{user.id}</p>
            </div>
          </div>

          <div className="flex items-start space-x-3">
            <Shield className="h-5 w-5 text-gray-500 mt-0.5" />
            <div>
              <p className="text-sm text-gray-500">Role Description</p>
              <p className="text-gray-900">{getRoleDescription(user.role)}</p>
            </div>
          </div>
        </div>

        <div className="border-t pt-6">
          <div className="flex flex-col sm:flex-row gap-3">
            <Button
              onClick={() => window.location.href = "/profile/edit"}
              variant="outline"
              className="flex-1"
            >
              Edit Profile
            </Button>
            <Button
              onClick={() => signOut({ callbackUrl: "/" })}
              variant="destructive"
              className="flex-1"
            >
              Sign Out
            </Button>
          </div>
        </div>
      </div>

      <div className="mt-6 bg-gray-50 rounded-lg p-4">
        <h3 className="font-medium text-gray-900 mb-2">Session Information</h3>
        <div className="text-sm text-gray-600 space-y-1">
          <p>Authentication Status: <span className="font-medium text-green-600">Authenticated</span></p>
          <p>Session Active: <span className="font-medium">Yes</span></p>
          <p>Last Updated: <span className="font-medium">{new Date().toLocaleString()}</span></p>
        </div>
      </div>
    </div>
  )
}