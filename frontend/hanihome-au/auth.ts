import NextAuth from "next-auth"
import Google from "next-auth/providers/google"
import { JWT } from "next-auth/jwt"
import { Session, User } from "next-auth"

export interface ExtendedUser extends User {
  id: string
  role: string
  accessToken?: string
  refreshToken?: string
}

export interface ExtendedSession extends Session {
  user: ExtendedUser
  accessToken?: string
  refreshToken?: string
}

export interface ExtendedJWT extends JWT {
  accessToken?: string
  refreshToken?: string
  role?: string
  userId?: string
  accessTokenExpires?: number
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
    }),
  ],
  
  pages: {
    signIn: "/auth/signin",
    signOut: "/auth/signout",
    error: "/auth/error",
  },

  callbacks: {
    async signIn({ user, account, profile }) {
      try {
        if (account?.provider === "google") {
          // Call backend to register/login user
          const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/auth/oauth2/callback/google`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              email: user.email,
              name: user.name,
              image: user.image,
              oauthId: account.providerAccountId,
              provider: account.provider,
            }),
          })

          if (response.ok) {
            const data = await response.json()
            
            // Store tokens for later use
            if (account) {
              account.access_token = data.data.accessToken
              account.refresh_token = data.data.refreshToken
            }
            
            // Add user role information
            user.role = data.data.role
            user.id = data.data.userId
            
            return true
          } else {
            console.error("Backend authentication failed:", await response.text())
            return false
          }
        }
        
        return true
      } catch (error) {
        console.error("Error during sign in:", error)
        return false
      }
    },

    async jwt({ token, account, user }) {
      // Initial sign in
      if (account && user) {
        const extendedUser = user as ExtendedUser
        return {
          ...token,
          accessToken: account.access_token,
          refreshToken: account.refresh_token,
          role: extendedUser.role,
          userId: extendedUser.id,
          accessTokenExpires: account.expires_at ? account.expires_at * 1000 : Date.now() + 60 * 60 * 1000, // 1 hour default
        } as ExtendedJWT
      }

      const extendedToken = token as ExtendedJWT

      // Return previous token if the access token has not expired yet
      if (extendedToken.accessTokenExpires && Date.now() < extendedToken.accessTokenExpires) {
        return extendedToken
      }

      // Access token has expired, try to refresh it
      return await refreshAccessToken(extendedToken)
    },

    async session({ session, token }) {
      const extendedToken = token as ExtendedJWT
      const extendedSession = session as ExtendedSession

      extendedSession.user.id = extendedToken.userId || ""
      extendedSession.user.role = extendedToken.role || ""
      extendedSession.accessToken = extendedToken.accessToken
      extendedSession.refreshToken = extendedToken.refreshToken

      return extendedSession
    },
  },

  session: {
    strategy: "jwt",
    maxAge: 24 * 60 * 60, // 24 hours
  },

  jwt: {
    maxAge: 24 * 60 * 60, // 24 hours
  },

  secret: process.env.NEXTAUTH_SECRET,
})

async function refreshAccessToken(token: ExtendedJWT): Promise<ExtendedJWT> {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        refreshToken: token.refreshToken,
      }),
    })

    const refreshedTokens = await response.json()

    if (!response.ok) {
      throw refreshedTokens
    }

    return {
      ...token,
      accessToken: refreshedTokens.data.accessToken,
      refreshToken: refreshedTokens.data.refreshToken || token.refreshToken,
      accessTokenExpires: Date.now() + (refreshedTokens.data.expiresIn * 1000),
    }
  } catch (error) {
    console.error("Error refreshing access token:", error)

    return {
      ...token,
      error: "RefreshAccessTokenError",
    }
  }
}