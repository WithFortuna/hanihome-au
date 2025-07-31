"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "../../../hooks/use-auth"
import { Button } from "../../../components/ui/button"
import { ArrowLeft, Save, Upload, Trash2, Shield, Eye, EyeOff } from "lucide-react"

interface UserProfile {
  id: number
  name: string
  email: string
  phone?: string
  address?: string
  bio?: string
  profileImageUrl?: string
  role: string
  enabled: boolean
  twoFactorEnabled: boolean
}

interface PasswordChangeData {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export default function ProfileEditPage() {
  const router = useRouter()
  const { user, isAuthenticated, isLoading } = useAuth()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [passwordData, setPasswordData] = useState<PasswordChangeData>({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  })
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false
  })
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")
  const [activeTab, setActiveTab] = useState<"profile" | "security">("profile")

  useEffect(() => {
    if (isAuthenticated && user) {
      fetchProfile()
    }
  }, [isAuthenticated, user])

  const fetchProfile = async () => {
    try {
      const response = await fetch("/api/profile", {
        credentials: "include"
      })
      if (response.ok) {
        const data = await response.json()
        setProfile(data)
      } else {
        setError("프로필을 불러올 수 없습니다")
      }
    } catch (err) {
      setError("프로필을 불러오는 중 오류가 발생했습니다")
    }
  }

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!profile) return

    setLoading(true)
    setMessage("")
    setError("")

    try {
      const response = await fetch("/api/profile", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          name: profile.name,
          phone: profile.phone,
          address: profile.address,
          bio: profile.bio
        }),
      })

      if (response.ok) {
        setMessage("프로필이 성공적으로 업데이트되었습니다")
      } else {
        const errorData = await response.json()
        setError(errorData.error || "프로필 업데이트에 실패했습니다")
      }
    } catch (err) {
      setError("프로필 업데이트 중 오류가 발생했습니다")
    } finally {
      setLoading(false)
    }
  }

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError("새 비밀번호와 확인 비밀번호가 일치하지 않습니다")
      return
    }

    if (passwordData.newPassword.length < 8) {
      setError("새 비밀번호는 최소 8자 이상이어야 합니다")
      return
    }

    setLoading(true)
    setMessage("")
    setError("")

    try {
      const response = await fetch("/api/profile/password/change", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.confirmPassword
        }),
      })

      if (response.ok) {
        setMessage("비밀번호가 성공적으로 변경되었습니다")
        setPasswordData({
          currentPassword: "",
          newPassword: "",
          confirmPassword: ""
        })
      } else {
        const errorData = await response.json()
        setError(errorData.error || "비밀번호 변경에 실패했습니다")
      }
    } catch (err) {
      setError("비밀번호 변경 중 오류가 발생했습니다")
    } finally {
      setLoading(false)
    }
  }

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    if (file.size > 5 * 1024 * 1024) {
      setError("파일 크기는 5MB를 초과할 수 없습니다")
      return
    }

    const formData = new FormData()
    formData.append("file", file)

    setLoading(true)
    setMessage("")
    setError("")

    try {
      const response = await fetch("/api/profile/image", {
        method: "POST",
        credentials: "include",
        body: formData,
      })

      if (response.ok) {
        const data = await response.json()
        setProfile(prev => prev ? { ...prev, profileImageUrl: data.imageUrl } : null)
        setMessage("프로필 이미지가 성공적으로 업로드되었습니다")
      } else {
        const errorData = await response.json()
        setError(errorData.error || "이미지 업로드에 실패했습니다")
      }
    } catch (err) {
      setError("이미지 업로드 중 오류가 발생했습니다")
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteImage = async () => {
    if (!confirm("프로필 이미지를 삭제하시겠습니까?")) return

    setLoading(true)
    setMessage("")
    setError("")

    try {
      const response = await fetch("/api/profile/image", {
        method: "DELETE",
        credentials: "include",
      })

      if (response.ok) {
        setProfile(prev => prev ? { ...prev, profileImageUrl: undefined } : null)
        setMessage("프로필 이미지가 성공적으로 삭제되었습니다")
      } else {
        const errorData = await response.json()
        setError(errorData.error || "이미지 삭제에 실패했습니다")
      }
    } catch (err) {
      setError("이미지 삭제 중 오류가 발생했습니다")
    } finally {
      setLoading(false)
    }
  }

  const toggleTwoFactor = async () => {
    if (!profile) return

    setLoading(true)
    setMessage("")
    setError("")

    try {
      const endpoint = profile.twoFactorEnabled ? "disable" : "enable"
      const response = await fetch(`/api/profile/two-factor/${endpoint}`, {
        method: "POST",
        credentials: "include",
      })

      if (response.ok) {
        setProfile(prev => prev ? { ...prev, twoFactorEnabled: !prev.twoFactorEnabled } : null)
        setMessage(`2단계 인증이 ${profile.twoFactorEnabled ? "비활성화" : "활성화"}되었습니다`)
      } else {
        const errorData = await response.json()
        setError(errorData.error || "2단계 인증 설정 변경에 실패했습니다")
      }
    } catch (err) {
      setError("2단계 인증 설정 변경 중 오류가 발생했습니다")
    } finally {
      setLoading(false)
    }
  }

  if (isLoading) {
    return <div className="flex items-center justify-center p-8">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
    </div>
  }

  if (!isAuthenticated || !profile) {
    return <div className="text-center p-8">
      <p className="text-gray-600">로그인이 필요합니다.</p>
    </div>
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="max-w-4xl mx-auto flex items-center space-x-4">
          <Button
            variant="ghost"
            onClick={() => router.back()}
            className="p-2"
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">프로필 편집</h1>
            <p className="text-gray-600">계정 정보 및 보안 설정을 관리하세요</p>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto p-4">
        {message && (
          <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-md">
            <p className="text-green-800">{message}</p>
          </div>
        )}

        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        <div className="bg-white rounded-lg shadow-md">
          <div className="flex border-b">
            <button
              className={`px-6 py-3 font-medium ${
                activeTab === "profile"
                  ? "text-blue-600 border-b-2 border-blue-600"
                  : "text-gray-500 hover:text-gray-700"
              }`}
              onClick={() => setActiveTab("profile")}
            >
              프로필 정보
            </button>
            <button
              className={`px-6 py-3 font-medium ${
                activeTab === "security"
                  ? "text-blue-600 border-b-2 border-blue-600"
                  : "text-gray-500 hover:text-gray-700"
              }`}
              onClick={() => setActiveTab("security")}
            >
              보안 설정
            </button>
          </div>

          <div className="p-6">
            {activeTab === "profile" && (
              <div className="space-y-6">
                {/* Profile Image Section */}
                <div className="flex items-center space-x-4">
                  <div className="h-20 w-20 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden">
                    {profile.profileImageUrl ? (
                      <img
                        src={profile.profileImageUrl}
                        alt="Profile"
                        className="h-20 w-20 object-cover"
                      />
                    ) : (
                      <div className="text-gray-500 text-2xl font-bold">
                        {profile.name.charAt(0).toUpperCase()}
                      </div>
                    )}
                  </div>
                  <div className="flex space-x-2">
                    <label className="cursor-pointer">
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageUpload}
                        className="hidden"
                      />
                      <Button variant="outline" size="sm" disabled={loading}>
                        <Upload className="h-4 w-4 mr-2" />
                        이미지 업로드
                      </Button>
                    </label>
                    {profile.profileImageUrl && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={handleDeleteImage}
                        disabled={loading}
                      >
                        <Trash2 className="h-4 w-4 mr-2" />
                        삭제
                      </Button>
                    )}
                  </div>
                </div>

                {/* Profile Form */}
                <form onSubmit={handleProfileSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        이름 *
                      </label>
                      <input
                        type="text"
                        required
                        value={profile.name}
                        onChange={(e) => setProfile(prev => prev ? { ...prev, name: e.target.value } : null)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        이메일
                      </label>
                      <input
                        type="email"
                        value={profile.email}
                        disabled
                        className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        전화번호
                      </label>
                      <input
                        type="tel"
                        value={profile.phone || ""}
                        onChange={(e) => setProfile(prev => prev ? { ...prev, phone: e.target.value } : null)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        역할
                      </label>
                      <input
                        type="text"
                        value={profile.role}
                        disabled
                        className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      주소
                    </label>
                    <input
                      type="text"
                      value={profile.address || ""}
                      onChange={(e) => setProfile(prev => prev ? { ...prev, address: e.target.value } : null)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      자기소개
                    </label>
                    <textarea
                      rows={4}
                      value={profile.bio || ""}
                      onChange={(e) => setProfile(prev => prev ? { ...prev, bio: e.target.value } : null)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="자신에 대해 간단히 소개해주세요..."
                    />
                  </div>

                  <div className="flex justify-end">
                    <Button type="submit" disabled={loading}>
                      <Save className="h-4 w-4 mr-2" />
                      {loading ? "저장 중..." : "프로필 저장"}
                    </Button>
                  </div>
                </form>
              </div>
            )}

            {activeTab === "security" && (
              <div className="space-y-6">
                {/* Two-Factor Authentication */}
                <div className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">2단계 인증</h3>
                      <p className="text-sm text-gray-600">
                        계정 보안을 위해 2단계 인증을 설정하세요
                      </p>
                    </div>
                    <Button
                      onClick={toggleTwoFactor}
                      disabled={loading}
                      variant={profile.twoFactorEnabled ? "destructive" : "default"}
                    >
                      <Shield className="h-4 w-4 mr-2" />
                      {profile.twoFactorEnabled ? "비활성화" : "활성화"}
                    </Button>
                  </div>
                  <div className="mt-2">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      profile.twoFactorEnabled 
                        ? "bg-green-100 text-green-800" 
                        : "bg-red-100 text-red-800"
                    }`}>
                      {profile.twoFactorEnabled ? "활성화됨" : "비활성화됨"}
                    </span>
                  </div>
                </div>

                {/* Password Change */}
                <div className="border border-gray-200 rounded-lg p-4">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">비밀번호 변경</h3>
                  <form onSubmit={handlePasswordSubmit} className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        현재 비밀번호
                      </label>
                      <div className="relative">
                        <input
                          type={showPasswords.current ? "text" : "password"}
                          required
                          value={passwordData.currentPassword}
                          onChange={(e) => setPasswordData(prev => ({ ...prev, currentPassword: e.target.value }))}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPasswords(prev => ({ ...prev, current: !prev.current }))}
                          className="absolute right-3 top-2 text-gray-400 hover:text-gray-600"
                        >
                          {showPasswords.current ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                        </button>
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        새 비밀번호
                      </label>
                      <div className="relative">
                        <input
                          type={showPasswords.new ? "text" : "password"}
                          required
                          minLength={8}
                          value={passwordData.newPassword}
                          onChange={(e) => setPasswordData(prev => ({ ...prev, newPassword: e.target.value }))}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPasswords(prev => ({ ...prev, new: !prev.new }))}
                          className="absolute right-3 top-2 text-gray-400 hover:text-gray-600"
                        >
                          {showPasswords.new ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                        </button>
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        새 비밀번호 확인
                      </label>
                      <div className="relative">
                        <input
                          type={showPasswords.confirm ? "text" : "password"}
                          required
                          value={passwordData.confirmPassword}
                          onChange={(e) => setPasswordData(prev => ({ ...prev, confirmPassword: e.target.value }))}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPasswords(prev => ({ ...prev, confirm: !prev.confirm }))}
                          className="absolute right-3 top-2 text-gray-400 hover:text-gray-600"
                        >
                          {showPasswords.confirm ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                        </button>
                      </div>
                    </div>

                    <div className="flex justify-end">
                      <Button type="submit" disabled={loading}>
                        {loading ? "변경 중..." : "비밀번호 변경"}
                      </Button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}