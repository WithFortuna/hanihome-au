import { UserProfile } from "../../components/auth/user-profile"
import { Metadata } from "next"

export const metadata: Metadata = {
  title: "Profile - HaniHome AU",
  description: "Manage your profile and account settings",
}

export default function ProfilePage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="max-w-6xl mx-auto">
          <h1 className="text-2xl font-bold text-gray-900">Profile</h1>
          <p className="text-gray-600">Manage your account information and settings</p>
        </div>
      </div>
      
      <div className="max-w-6xl mx-auto p-4">
        <UserProfile />
      </div>
    </div>
  )
}