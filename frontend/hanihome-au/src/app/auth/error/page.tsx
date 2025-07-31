"use client"

import { useSearchParams, useRouter } from "next/navigation"
import { Button } from "../../../components/ui/button"
import { AlertCircle, Home, RefreshCw } from "lucide-react"

const errorMessages: { [key: string]: string } = {
  Configuration: "There is a problem with the server configuration.",
  AccessDenied: "Access denied. You do not have permission to sign in.",
  Verification: "The verification token has expired or has already been used.",
  Default: "An error occurred during authentication. Please try again.",
}

export default function AuthErrorPage() {
  const searchParams = useSearchParams()
  const error = searchParams.get("error")
  const router = useRouter()

  const errorMessage = error ? errorMessages[error] || errorMessages.Default : errorMessages.Default

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <AlertCircle className="h-16 w-16 text-red-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-gray-900">
            Authentication Error
          </h1>
          <p className="mt-2 text-gray-600">
            {errorMessage}
          </p>
          {error && (
            <p className="mt-1 text-sm text-gray-500">
              Error code: {error}
            </p>
          )}
        </div>

        <div className="bg-white p-8 rounded-lg shadow-md border">
          <div className="space-y-4">
            <Button
              onClick={() => router.push("/auth/signin")}
              className="w-full"
              size="lg"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Try Again
            </Button>

            <Button
              onClick={() => router.push("/")}
              variant="outline"
              className="w-full"
              size="lg"
            >
              <Home className="h-4 w-4 mr-2" />
              Go Home
            </Button>
          </div>
        </div>

        <div className="text-center text-sm text-gray-500">
          <p>
            If this problem persists, please{" "}
            <a href="/contact" className="text-blue-600 hover:underline">
              contact support
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}