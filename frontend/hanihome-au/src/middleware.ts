import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"
import { auth } from "../auth"

// Define protected routes and their required roles
const protectedRoutes = {
  "/dashboard": ["TENANT", "LANDLORD", "AGENT", "ADMIN"],
  "/profile": ["TENANT", "LANDLORD", "AGENT", "ADMIN"],
  "/properties/create": ["LANDLORD", "AGENT", "ADMIN"],
  "/properties/manage": ["LANDLORD", "AGENT", "ADMIN"],
  "/applications": ["TENANT", "LANDLORD", "AGENT", "ADMIN"],
  "/admin": ["ADMIN"],
  "/agent": ["AGENT", "ADMIN"],
  "/landlord": ["LANDLORD", "ADMIN"],
  "/tenant": ["TENANT", "ADMIN"],
}

// Define public routes that don't require authentication
const publicRoutes = [
  "/",
  "/auth/signin",
  "/auth/signout",
  "/auth/error",
  "/properties",
  "/search",
  "/about",
  "/contact",
  "/terms",
  "/privacy",
  "/unauthorized",
]

// API routes that are always public
const publicApiRoutes = [
  "/api/auth",
  "/api/public",
  "/api/health",
]

export default auth((req) => {
  const { pathname } = req.nextUrl
  const isLoggedIn = !!req.auth
  const userRole = req.auth?.user?.role
  
  // Allow static files and Next.js internals
  if (pathname.startsWith("/_next/") || pathname.includes(".")) {
    return NextResponse.next()
  }

  // Check if it's a public API route
  const isPublicApiRoute = publicApiRoutes.some(route => 
    pathname.startsWith(route)
  )

  // Allow public API routes
  if (isPublicApiRoute) {
    return NextResponse.next()
  }

  // Handle protected API routes
  if (pathname.startsWith("/api/")) {
    // All other API routes require authentication
    if (!isLoggedIn) {
      return new NextResponse(
        JSON.stringify({ 
          success: false, 
          message: "인증이 필요한 API입니다.", 
          error: "UNAUTHORIZED",
          timestamp: new Date().toISOString()
        }),
        { 
          status: 401, 
          headers: { 
            "Content-Type": "application/json" 
          } 
        }
      )
    }

    // Check role-based API access
    if (pathname.startsWith("/api/v1/admin") && userRole !== "ADMIN") {
      return new NextResponse(
        JSON.stringify({ 
          success: false, 
          message: "관리자 권한이 필요합니다.", 
          error: "FORBIDDEN",
          requiredRoles: ["ADMIN"],
          userRole,
          timestamp: new Date().toISOString()
        }),
        { 
          status: 403, 
          headers: { 
            "Content-Type": "application/json" 
          } 
        }
      )
    }

    if (pathname.startsWith("/api/v1/agent") && !["AGENT", "ADMIN"].includes(userRole || "")) {
      return new NextResponse(
        JSON.stringify({ 
          success: false, 
          message: "에이전트 또는 관리자 권한이 필요합니다.", 
          error: "FORBIDDEN",
          requiredRoles: ["AGENT", "ADMIN"],
          userRole,
          timestamp: new Date().toISOString()
        }),
        { 
          status: 403, 
          headers: { 
            "Content-Type": "application/json" 
          } 
        }
      )
    }

    if (pathname.startsWith("/api/v1/landlord") && !["LANDLORD", "ADMIN"].includes(userRole || "")) {
      return new NextResponse(
        JSON.stringify({ 
          success: false, 
          message: "집주인 또는 관리자 권한이 필요합니다.", 
          error: "FORBIDDEN",
          requiredRoles: ["LANDLORD", "ADMIN"],
          userRole,
          timestamp: new Date().toISOString()
        }),
        { 
          status: 403, 
          headers: { 
            "Content-Type": "application/json" 
          } 
        }
      )
    }

    return NextResponse.next()
  }

  // Allow public routes
  if (publicRoutes.some(route => pathname === route || pathname.startsWith(route + "/"))) {
    return NextResponse.next()
  }

  // Check if route requires authentication
  const matchedRoute = Object.entries(protectedRoutes).find(([route]) => 
    pathname === route || pathname.startsWith(route + "/")
  )

  if (matchedRoute) {
    const [, requiredRoles] = matchedRoute
    
    // Redirect to sign-in if not authenticated
    if (!isLoggedIn) {
      const signInUrl = new URL("/auth/signin", req.url)
      signInUrl.searchParams.set("callbackUrl", pathname)
      return NextResponse.redirect(signInUrl)
    }

    // Check role-based access
    if (userRole && !requiredRoles.includes(userRole)) {
      // Create unauthorized URL with context
      const unauthorizedUrl = new URL("/unauthorized", req.url)
      unauthorizedUrl.searchParams.set("requiredRoles", requiredRoles.join(","))
      unauthorizedUrl.searchParams.set("userRole", userRole)
      unauthorizedUrl.searchParams.set("attemptedPath", pathname)
      
      return NextResponse.redirect(unauthorizedUrl)
    }
  }

  // Add security headers for authenticated requests
  const response = NextResponse.next()
  
  if (isLoggedIn && userRole) {
    response.headers.set("X-User-Role", userRole)
    response.headers.set("X-User-Authenticated", "true")
  }

  return response
})

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!api|_next/static|_next/image|favicon.ico).*)",
  ],
}