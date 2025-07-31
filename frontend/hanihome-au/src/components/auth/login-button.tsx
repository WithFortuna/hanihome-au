"use client"

import { signIn, signOut } from "next-auth/react"
import { Button } from "../ui/button"
import { useAuth } from "../../hooks/use-auth"
import { User, LogIn, LogOut } from "lucide-react"

interface LoginButtonProps {
  variant?: "default" | "outline" | "ghost"
  size?: "default" | "sm" | "lg"
  className?: string
}

export function LoginButton({ variant = "default", size = "default", className }: LoginButtonProps) {
  const { isAuthenticated, isLoading, user } = useAuth()

  if (isLoading) {
    return (
      <Button variant="ghost" size={size} className={className} disabled>
        Loading...
      </Button>
    )
  }

  if (isAuthenticated) {
    return (
      <div className="flex items-center gap-2">
        <div className="flex items-center gap-2 text-sm">
          <User className="h-4 w-4" />
          <span>{user?.name}</span>
          <span className="text-xs bg-gray-100 px-2 py-1 rounded">
            {user?.role}
          </span>
        </div>
        <Button
          variant={variant}
          size={size}
          className={className}
          onClick={() => signOut({ callbackUrl: "/" })}
        >
          <LogOut className="h-4 w-4 mr-2" />
          Sign Out
        </Button>
      </div>
    )
  }

  return (
    <Button
      variant={variant}
      size={size}
      className={className}
      onClick={() => signIn("google", { callbackUrl: "/dashboard" })}
    >
      <LogIn className="h-4 w-4 mr-2" />
      Sign In with Google
    </Button>
  )
}