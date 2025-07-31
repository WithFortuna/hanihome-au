"use client"

import { useEffect, useState } from "react"
import { signOut } from "next-auth/react"
import { useRouter } from "next/navigation"
import { Button } from "../../../components/ui/button"
import { LogOut, CheckCircle } from "lucide-react"

export default function SignOutPage() {
  const [isSigningOut, setIsSigningOut] = useState(false)
  const [isSignedOut, setIsSignedOut] = useState(false)
  const router = useRouter()

  const handleSignOut = async () => {
    try {
      setIsSigningOut(true)
      await signOut({ redirect: false })
      setIsSignedOut(true)
      
      // Redirect to home page after a short delay
      setTimeout(() => {
        router.push("/")
      }, 2000)
    } catch (error) {
      console.error("Sign out error:", error)
      setIsSigningOut(false)
    }
  }

  if (isSignedOut) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-4">
        <div className="text-center space-y-4">
          <CheckCircle className="h-16 w-16 text-green-600 mx-auto" />
          <h1 className="text-2xl font-bold text-gray-900">
            Successfully Signed Out
          </h1>
          <p className="text-gray-600">
            You have been successfully signed out of your account.
          </p>
          <p className="text-sm text-gray-500">
            Redirecting to home page...
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <LogOut className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-900">
            Sign Out
          </h1>
          <p className="mt-2 text-gray-600">
            Are you sure you want to sign out of your account?
          </p>
        </div>

        <div className="bg-white p-8 rounded-lg shadow-md border">
          <div className="space-y-4">
            <Button
              onClick={handleSignOut}
              disabled={isSigningOut}
              className="w-full"
              size="lg"
              variant="destructive"
            >
              {isSigningOut ? (
                <div className="flex items-center gap-2">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Signing out...</span>
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <LogOut className="h-4 w-4" />
                  <span>Sign Out</span>
                </div>
              )}
            </Button>

            <Button
              onClick={() => router.back()}
              disabled={isSigningOut}
              className="w-full"
              variant="outline"
              size="lg"
            >
              Cancel
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}