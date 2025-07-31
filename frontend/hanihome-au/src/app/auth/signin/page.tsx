import { LoginForm } from "../../../components/auth/login-form"
import { Metadata } from "next"

export const metadata: Metadata = {
  title: "Sign In - HaniHome AU",
  description: "Sign in to your HaniHome AU account",
}

export default function SignInPage() {
  return <LoginForm />
}