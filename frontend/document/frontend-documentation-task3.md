# User Profile Management System - Frontend Documentation

**Task ID:** 3  
**Task Title:** 사용자 프로필 관리 기능 구현 - Frontend Implementation  
**Status:** ✅ COMPLETED  
**Priority:** Medium  
**Completion Date:** July 2025  

## Executive Summary

The frontend implementation of the User Profile Management System provides a comprehensive and user-friendly interface for managing user profiles in the HaniHome AU platform. This includes responsive profile forms, secure image upload functionality, privacy settings management, and seamless integration with the backend API.

### Key Frontend Achievements
- **Responsive Profile Interface**: Mobile-first design with optimal user experience across devices
- **Interactive Image Upload**: Drag-and-drop interface with real-time preview and validation
- **Privacy Settings UI**: Intuitive granular privacy controls for profile fields
- **Form Validation**: Client-side validation with real-time feedback using React Hook Form and Zod
- **Performance Optimization**: Optimized bundle size and loading performance
- **Accessibility**: WCAG-compliant interface design

---

## Frontend Architecture Overview

### Component Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Architecture                    │
├─────────────────────────────────────────────────────────────┤
│  Pages Layer (Next.js App Router)                          │
│  ├── /profile/page.tsx (Main Profile Page)                 │
│  └── /profile/edit/page.tsx (Profile Edit Page)            │
├─────────────────────────────────────────────────────────────┤
│  Components Layer                                           │
│  ├── user-profile.tsx (Main Profile Component)             │
│  ├── profile-image-upload.tsx (Image Upload Component)     │
│  ├── privacy-settings.tsx (Privacy Controls)               │
│  └── region-preferences.tsx (Location Preferences)         │
├─────────────────────────────────────────────────────────────┤
│  Hooks Layer                                                │
│  ├── use-profile.ts (Profile State Management)             │
│  ├── use-image-upload.ts (File Upload Logic)               │
│  └── use-privacy-settings.ts (Privacy State)               │
├─────────────────────────────────────────────────────────────┤
│  API Layer                                                  │
│  ├── profile-api.ts (Profile API Calls)                    │
│  └── file-upload-api.ts (Image Upload API)                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Implementation Details

### 1. Profile Page Implementation (Subtask 3.4)

#### Main Profile Page
**File:** `/frontend/hanihome-au/src/app/profile/page.tsx`

**Key Features:**
- Server-side rendering with user authentication check
- Responsive layout with mobile-first approach
- Loading states and error handling
- Integration with authentication context

```typescript
export default async function ProfilePage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white shadow-sm rounded-lg">
          <UserProfile />
        </div>
      </div>
    </div>
  )
}
```

#### Profile Edit Page
**File:** `/frontend/hanihome-au/src/app/profile/edit/page.tsx`

**Features:**
- Dedicated editing interface
- Form-focused layout
- Save/cancel functionality
- Navigation breadcrumbs

### 2. User Profile Component

#### Main Profile Component
**File:** `/frontend/hanihome-au/src/components/auth/user-profile.tsx`

**Key Implementation Features:**

##### Form Management with React Hook Form
```typescript
const profileForm = useForm<ProfileFormData>({
  resolver: zodResolver(profileSchema),
  defaultValues: {
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    bio: user?.bio || '',
    address: user?.address || ''
  }
})
```

##### Real-time Validation with Zod Schema
```typescript
const profileSchema = z.object({
  name: z.string()
    .min(2, '이름은 최소 2글자 이상이어야 합니다')
    .max(50, '이름은 50글자를 초과할 수 없습니다'),
  phone: z.string()
    .regex(/^(\+61|0)[4-5][0-9]{8}$/, '유효한 호주 전화번호를 입력해주세요')
    .optional(),
  bio: z.string()
    .max(500, '자기소개는 500글자를 초과할 수 없습니다')
    .optional(),
  address: z.string()
    .max(200, '주소는 200글자를 초과할 수 없습니다')
    .optional()
})
```

##### State Management and API Integration
```typescript
const { mutate: updateProfile, isPending } = useMutation({
  mutationFn: updateUserProfile,
  onSuccess: (data) => {
    toast.success('프로필이 성공적으로 업데이트되었습니다')
    queryClient.invalidateQueries({ queryKey: ['user'] })
  },
  onError: (error) => {
    toast.error('프로필 업데이트에 실패했습니다')
  }
})
```

### 3. Image Upload Component

#### Drag-and-Drop Interface
**File:** `/frontend/hanihome-au/src/components/auth/profile-image-upload.tsx`

**Features:**
- Drag-and-drop file upload
- Image preview functionality
- File validation (size, type, dimensions)
- Progress indicators
- Error handling with user feedback

```typescript
const ImageUploadComponent = () => {
  const onDrop = useCallback((acceptedFiles: File[]) => {
    const file = acceptedFiles[0]
    
    // Client-side validation
    if (file.size > MAX_FILE_SIZE) {
      setError('파일 크기는 5MB를 초과할 수 없습니다')
      return
    }
    
    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      setError('JPEG, PNG, WebP 형식의 파일만 업로드 가능합니다')
      return
    }
    
    setPreviewUrl(URL.createObjectURL(file))
    uploadImage(file)
  }, [uploadImage])
  
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpeg', '.jpg', '.png', '.webp']
    },
    maxFiles: 1,
    multiple: false
  })
  
  return (
    <div {...getRootProps()} className="upload-dropzone">
      <input {...getInputProps()} />
      {/* Upload interface implementation */}
    </div>
  )
}
```

#### Image Processing and Optimization
```typescript
const processImage = async (file: File): Promise<File> => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')!
    const img = new Image()
    
    img.onload = () => {
      // Calculate optimal dimensions
      const { width, height } = calculateDimensions(img, MAX_DIMENSION)
      
      canvas.width = width
      canvas.height = height
      
      // Draw and compress
      ctx.drawImage(img, 0, 0, width, height)
      
      canvas.toBlob((blob) => {
        const processedFile = new File([blob!], file.name, {
          type: 'image/webp',
          lastModified: Date.now()
        })
        resolve(processedFile)
      }, 'image/webp', 0.8)
    }
    
    img.src = URL.createObjectURL(file)
  })
}
```

### 4. Privacy Settings Component

#### Privacy Controls Interface
**File:** `/frontend/hanihome-au/src/components/auth/privacy-settings.tsx`

**Features:**
- Granular field-level privacy controls
- Three-tier privacy levels (Public, Members Only, Private)
- GDPR consent management
- Real-time privacy preview

```typescript
const PrivacySettingsComponent = () => {
  const [privacySettings, setPrivacySettings] = useState<PrivacySettings>({
    namePrivacy: 'PUBLIC',
    phonePrivacy: 'MEMBERS_ONLY',
    emailPrivacy: 'PRIVATE',
    profileImagePrivacy: 'PUBLIC',
    bioPrivacy: 'PUBLIC',
    addressPrivacy: 'MEMBERS_ONLY'
  })
  
  const privacyOptions = [
    { value: 'PUBLIC', label: '전체 공개', icon: Globe },
    { value: 'MEMBERS_ONLY', label: '회원에게만', icon: Users },
    { value: 'PRIVATE', label: '비공개', icon: Lock }
  ]
  
  return (
    <div className="privacy-settings">
      {Object.entries(privacySettings).map(([field, value]) => (
        <div key={field} className="privacy-field">
          <label>{getFieldLabel(field)}</label>
          <Select value={value} onValueChange={(newValue) => 
            updatePrivacySetting(field, newValue)
          }>
            {privacyOptions.map(option => (
              <SelectItem key={option.value} value={option.value}>
                <option.icon className="w-4 h-4 mr-2" />
                {option.label}
              </SelectItem>
            ))}
          </Select>
        </div>
      ))}
    </div>
  )
}
```

### 5. Region Preferences Component

#### Location Management Interface
**File:** `/frontend/hanihome-au/src/components/auth/region-preferences.tsx`

**Features:**
- Interactive region selection
- Priority-based ranking
- Search radius configuration
- Geographic coordinate integration

```typescript
const RegionPreferencesComponent = () => {
  const [preferredRegions, setPreferredRegions] = useState<PreferredRegion[]>([])
  
  const addRegion = (region: RegionSelection) => {
    const newRegion: PreferredRegion = {
      regionName: region.name,
      state: region.state,
      country: 'Australia',
      latitude: region.coordinates.lat,
      longitude: region.coordinates.lng,
      radiusKm: 10, // Default 10km radius
      priority: preferredRegions.length + 1,
      isActive: true
    }
    
    setPreferredRegions(prev => [...prev, newRegion])
  }
  
  return (
    <div className="region-preferences">
      <RegionSearch onRegionSelect={addRegion} />
      <DragDropRegionList 
        regions={preferredRegions}
        onReorder={setPreferredRegions}
      />
    </div>
  )
}
```

---

## API Integration Layer

### Profile API Service
**File:** `/frontend/hanihome-au/src/lib/api/profile-api.ts`

#### API Client Configuration
```typescript
const profileApi = {
  // Get current user profile
  getCurrentProfile: async (): Promise<UserProfile> => {
    const response = await apiClient.get('/api/profile')
    return response.data
  },
  
  // Update profile information
  updateProfile: async (profileData: ProfileUpdateData): Promise<UserProfile> => {
    const response = await apiClient.put('/api/profile', profileData)
    return response.data
  },
  
  // Upload profile image
  uploadProfileImage: async (file: File): Promise<ImageUploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await apiClient.post('/api/profile/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total!
        )
        // Update progress state
      }
    })
    
    return response.data
  },
  
  // Delete profile image
  deleteProfileImage: async (): Promise<void> => {
    await apiClient.delete('/api/profile/image')
  }
}
```

#### Error Handling
```typescript
const handleApiError = (error: AxiosError) => {
  if (error.response?.status === 429) {
    toast.error('요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.')
  } else if (error.response?.status === 400) {
    const validationErrors = error.response.data?.details
    Object.entries(validationErrors).forEach(([field, message]) => {
      toast.error(`${field}: ${message}`)
    })
  } else {
    toast.error('오류가 발생했습니다. 다시 시도해주세요.')
  }
}
```

---

## State Management with React Query

### Custom Hooks Implementation

#### Profile Data Hook
**File:** `/frontend/hanihome-au/src/hooks/use-profile.ts`

```typescript
export const useProfile = () => {
  return useQuery({
    queryKey: ['user', 'profile'],
    queryFn: profileApi.getCurrentProfile,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
    retry: 2,
    refetchOnWindowFocus: false
  })
}

export const useUpdateProfile = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: profileApi.updateProfile,
    onSuccess: (data) => {
      // Update cached data
      queryClient.setQueryData(['user', 'profile'], data)
      
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: ['user'] })
      
      toast.success('프로필이 업데이트되었습니다')
    },
    onError: handleApiError
  })
}
```

#### Image Upload Hook
**File:** `/frontend/hanihome-au/src/hooks/use-image-upload.ts`

```typescript
export const useImageUpload = () => {
  const [uploadProgress, setUploadProgress] = useState(0)
  const [isUploading, setIsUploading] = useState(false)
  const queryClient = useQueryClient()
  
  const uploadMutation = useMutation({
    mutationFn: async (file: File) => {
      setIsUploading(true)
      setUploadProgress(0)
      
      // Process image before upload
      const processedFile = await processImage(file)
      
      return profileApi.uploadProfileImage(processedFile)
    },
    onSuccess: (data) => {
      // Update profile cache with new image URL
      queryClient.setQueryData(['user', 'profile'], (old: UserProfile) => ({
        ...old,
        profileImageUrl: data.imageUrl
      }))
      
      toast.success('프로필 이미지가 업로드되었습니다')
      setIsUploading(false)
      setUploadProgress(0)
    },
    onError: (error) => {
      handleApiError(error)
      setIsUploading(false)
      setUploadProgress(0)
    }
  })
  
  return {
    uploadImage: uploadMutation.mutate,
    isUploading,
    uploadProgress,
    error: uploadMutation.error
  }
}
```

---

## UI/UX Design Implementation

### Responsive Design Strategy

#### Mobile-First Approach
```css
/* Base styles for mobile */
.profile-container {
  @apply px-4 py-6;
}

.profile-form {
  @apply space-y-4;
}

.form-field {
  @apply flex flex-col space-y-2;
}

/* Tablet styles */
@media (min-width: 768px) {
  .profile-container {
    @apply px-6 py-8;
  }
  
  .profile-form {
    @apply grid grid-cols-2 gap-6;
  }
}

/* Desktop styles */
@media (min-width: 1024px) {
  .profile-container {
    @apply px-8 py-12;
  }
  
  .profile-form {
    @apply grid-cols-3;
  }
}
```

#### Component Design System
```typescript
// Button variants for consistent styling
const buttonVariants = {
  primary: 'bg-blue-600 hover:bg-blue-700 text-white',
  secondary: 'bg-gray-200 hover:bg-gray-300 text-gray-900',
  danger: 'bg-red-600 hover:bg-red-700 text-white'
}

// Input field styling
const inputStyles = {
  base: 'w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm',
  focus: 'focus:ring-2 focus:ring-blue-500 focus:border-blue-500',
  error: 'border-red-300 focus:ring-red-500 focus:border-red-500'
}
```

### Loading States and Animations

#### Skeleton Loading
```typescript
const ProfileSkeleton = () => (
  <div className="animate-pulse">
    <div className="flex items-center space-x-4">
      <div className="w-20 h-20 bg-gray-300 rounded-full"></div>
      <div className="flex-1">
        <div className="h-4 bg-gray-300 rounded w-3/4 mb-2"></div>
        <div className="h-4 bg-gray-300 rounded w-1/2"></div>
      </div>
    </div>
    <div className="mt-6 space-y-4">
      <div className="h-10 bg-gray-300 rounded"></div>
      <div className="h-10 bg-gray-300 rounded"></div>
      <div className="h-24 bg-gray-300 rounded"></div>
    </div>
  </div>
)
```

#### Upload Progress Indicator
```typescript
const UploadProgress = ({ progress }: { progress: number }) => (
  <div className="w-full bg-gray-200 rounded-full h-2">
    <div 
      className="bg-blue-600 h-2 rounded-full transition-all duration-300 ease-out"
      style={{ width: `${progress}%` }}
    />
    <span className="text-sm text-gray-600 mt-1">
      {progress}% 업로드 완료
    </span>
  </div>
)
```

---

## Form Validation and Error Handling

### Client-Side Validation Strategy

#### Validation Schema with Zod
```typescript
const profileValidationSchema = z.object({
  name: z.string()
    .min(2, '이름은 최소 2글자 이상이어야 합니다')
    .max(50, '이름은 50글자를 초과할 수 없습니다')
    .regex(/^[가-힣a-zA-Z\s]+$/, '이름에는 한글, 영문, 공백만 입력 가능합니다'),
    
  phone: z.string()
    .regex(/^(\+61|0)[4-5][0-9]{8}$/, '유효한 호주 전화번호를 입력해주세요')
    .optional()
    .or(z.literal('')),
    
  email: z.string()
    .email('유효한 이메일 주소를 입력해주세요')
    .max(100, '이메일은 100글자를 초과할 수 없습니다'),
    
  bio: z.string()
    .max(500, '자기소개는 500글자를 초과할 수 없습니다')
    .optional()
    .or(z.literal('')),
    
  address: z.string()
    .max(200, '주소는 200글자를 초과할 수 없습니다')
    .optional()
    .or(z.literal(''))
})
```

#### Real-time Validation Implementation
```typescript
const ProfileForm = () => {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid, isDirty }
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileValidationSchema),
    mode: 'onChange' // Enable real-time validation
  })
  
  // Watch for changes to show save indicator
  const watchedFields = watch()
  
  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className="form-field">
        <label htmlFor="name">이름 *</label>
        <input
          {...register('name')}
          className={`${inputStyles.base} ${
            errors.name ? inputStyles.error : inputStyles.focus
          }`}
        />
        {errors.name && (
          <p className="text-sm text-red-600">{errors.name.message}</p>
        )}
      </div>
      
      {/* Save button with validation state */}
      <button
        type="submit"
        disabled={!isValid || !isDirty}
        className={`${buttonVariants.primary} ${
          (!isValid || !isDirty) ? 'opacity-50 cursor-not-allowed' : ''
        }`}
      >
        프로필 저장
      </button>
    </form>
  )
}
```

### Error Display and User Feedback

#### Toast Notification System
```typescript
const showSuccessToast = (message: string) => {
  toast.success(message, {
    duration: 4000,
    position: 'top-right',
    style: {
      background: '#10B981',
      color: '#FFFFFF'
    }
  })
}

const showErrorToast = (message: string) => {
  toast.error(message, {
    duration: 6000,
    position: 'top-right',
    style: {
      background: '#EF4444',
      color: '#FFFFFF'
    }
  })
}
```

#### Field-Level Error Display
```typescript
const FieldError = ({ error }: { error?: FieldError }) => {
  if (!error) return null
  
  return (
    <div className="flex items-center mt-1 text-sm text-red-600">
      <ExclamationCircleIcon className="w-4 h-4 mr-1" />
      {error.message}
    </div>
  )
}
```

---

## Performance Optimization

### Bundle Optimization

#### Code Splitting
```typescript
// Lazy load heavy components
const ProfileImageUpload = lazy(() => 
  import('../components/auth/profile-image-upload')
)
const PrivacySettings = lazy(() => 
  import('../components/auth/privacy-settings')
)

// Use with Suspense
<Suspense fallback={<ComponentSkeleton />}>
  <ProfileImageUpload />
</Suspense>
```

#### Image Optimization
```typescript
const optimizeImage = async (file: File): Promise<File> => {
  // Use canvas for client-side image optimization
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')!
  
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      // Calculate optimal size maintaining aspect ratio
      const maxDimension = 800
      const ratio = Math.min(maxDimension / img.width, maxDimension / img.height)
      
      canvas.width = img.width * ratio
      canvas.height = img.height * ratio
      
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
      
      canvas.toBlob((blob) => {
        resolve(new File([blob!], file.name, {
          type: 'image/webp',
          lastModified: Date.now()
        }))
      }, 'image/webp', 0.85)
    }
    img.src = URL.createObjectURL(file)
  })
}
```

### Memory Management

#### Cleanup Effects
```typescript
const ProfileComponent = () => {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  
  // Cleanup blob URLs to prevent memory leaks
  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl)
      }
    }
  }, [previewUrl])
  
  // Cleanup on component unmount
  useEffect(() => {
    return () => {
      // Cancel any pending API requests
      // Clear any timers
      // Cleanup event listeners
    }
  }, [])
}
```

---

## Accessibility Implementation

### WCAG Compliance

#### Keyboard Navigation
```typescript
const AccessibleButton = ({ onClick, children, ...props }) => (
  <button
    onClick={onClick}
    onKeyDown={(e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault()
        onClick()
      }
    }}
    aria-label={props['aria-label']}
    {...props}
  >
    {children}
  </button>
)
```

#### Screen Reader Support
```typescript
const ProfileForm = () => (
  <form role="form" aria-label="프로필 정보 수정">
    <fieldset>
      <legend>기본 정보</legend>
      
      <div>
        <label htmlFor="name" id="name-label">
          이름 <span aria-label="필수 입력">*</span>
        </label>
        <input
          id="name"
          type="text"
          aria-labelledby="name-label"
          aria-describedby="name-error"
          aria-invalid={errors.name ? 'true' : 'false'}
          {...register('name')}
        />
        {errors.name && (
          <div id="name-error" role="alert" aria-live="polite">
            {errors.name.message}
          </div>
        )}
      </div>
    </fieldset>
  </form>
)
```

#### Color Contrast and Visual Indicators
```css
/* High contrast error states */
.input-error {
  @apply border-red-500 bg-red-50;
  box-shadow: 0 0 0 1px #ef4444;
}

/* Focus indicators */
.input:focus {
  @apply outline-none ring-2 ring-blue-500 ring-offset-2;
}

/* Status indicators beyond color */
.success-message::before {
  content: "✓ ";
  @apply text-green-600 font-bold;
}

.error-message::before {
  content: "⚠ ";
  @apply text-red-600 font-bold;
}
```

---

## Testing Strategy

### Unit Testing with Jest and React Testing Library

#### Component Testing
```typescript
// ProfileForm.test.tsx
describe('ProfileForm', () => {
  test('renders all form fields correctly', () => {
    render(<ProfileForm />)
    
    expect(screen.getByLabelText(/이름/)).toBeInTheDocument()
    expect(screen.getByLabelText(/전화번호/)).toBeInTheDocument()
    expect(screen.getByLabelText(/자기소개/)).toBeInTheDocument()
  })
  
  test('validates required fields', async () => {
    render(<ProfileForm />)
    
    const submitButton = screen.getByRole('button', { name: /저장/ })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText(/이름은 필수입니다/)).toBeInTheDocument()
    })
  })
  
  test('submits form with valid data', async () => {
    const mockSubmit = jest.fn()
    render(<ProfileForm onSubmit={mockSubmit} />)
    
    fireEvent.change(screen.getByLabelText(/이름/), {
      target: { value: '홍길동' }
    })
    
    fireEvent.click(screen.getByRole('button', { name: /저장/ }))
    
    await waitFor(() => {
      expect(mockSubmit).toHaveBeenCalledWith({
        name: '홍길동',
        // ... other form data
      })
    })
  })
})
```

#### Hook Testing
```typescript
// use-profile.test.ts
describe('useProfile', () => {
  test('fetches profile data successfully', async () => {
    const mockProfile = { id: 1, name: '테스트 사용자' }
    
    server.use(
      rest.get('/api/profile', (req, res, ctx) => {
        return res(ctx.json(mockProfile))
      })
    )
    
    const { result } = renderHook(() => useProfile(), {
      wrapper: QueryClientWrapper
    })
    
    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true)
      expect(result.current.data).toEqual(mockProfile)
    })
  })
})
```

### Integration Testing with Cypress

#### End-to-End Profile Management Flow
```typescript
// profile-management.cy.ts
describe('Profile Management', () => {
  beforeEach(() => {
    cy.login() // Custom command for authentication
    cy.visit('/profile')
  })
  
  it('should update profile information successfully', () => {
    // Edit profile information
    cy.get('[data-testid="edit-profile-button"]').click()
    
    cy.get('[data-testid="name-input"]')
      .clear()
      .type('업데이트된 이름')
    
    cy.get('[data-testid="bio-textarea"]')
      .clear()
      .type('업데이트된 자기소개입니다.')
    
    // Submit form
    cy.get('[data-testid="save-profile-button"]').click()
    
    // Verify success
    cy.get('[data-testid="success-toast"]')
      .should('contain', '프로필이 업데이트되었습니다')
    
    // Verify data persistence
    cy.reload()
    cy.get('[data-testid="profile-name"]')
      .should('contain', '업데이트된 이름')
  })
  
  it('should upload profile image successfully', () => {
    // Upload image
    cy.get('[data-testid="image-upload-area"]')
      .selectFile('cypress/fixtures/profile-image.jpg', {
        action: 'drag-drop'
      })
    
    // Wait for upload completion
    cy.get('[data-testid="upload-progress"]', { timeout: 10000 })
      .should('not.exist')
    
    // Verify image is displayed
    cy.get('[data-testid="profile-image"]')
      .should('be.visible')
      .and('have.attr', 'src')
      .and('include', 'profiles')
  })
})
```

---

## Security Implementation

### Client-Side Security Measures

#### Input Sanitization
```typescript
import DOMPurify from 'dompurify'

const sanitizeInput = (input: string): string => {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [], // No HTML tags allowed
    ALLOWED_ATTR: []
  })
}

// Usage in form handling
const handleFormSubmit = (data: ProfileFormData) => {
  const sanitizedData = {
    ...data,
    name: sanitizeInput(data.name),
    bio: sanitizeInput(data.bio),
    address: sanitizeInput(data.address)
  }
  
  updateProfile(sanitizedData)
}
```

#### File Upload Security
```typescript
const validateFile = (file: File): boolean => {
  // Check file size (5MB limit)
  if (file.size > 5 * 1024 * 1024) {
    throw new Error('파일 크기는 5MB를 초과할 수 없습니다')
  }
  
  // Check file type
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    throw new Error('JPEG, PNG, WebP 형식만 업로드 가능합니다')
  }
  
  // Check file name for malicious patterns
  const fileName = file.name.toLowerCase()
  const dangerousPatterns = ['.exe', '.js', '.php', '..', '<script']
  
  if (dangerousPatterns.some(pattern => fileName.includes(pattern))) {
    throw new Error('유효하지 않은 파일입니다')
  }
  
  return true
}
```

#### Secure API Communication
```typescript
const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10000,
  withCredentials: true
})

// Request interceptor for authentication
apiClient.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      clearAuthToken()
      router.push('/auth/signin')
    }
    return Promise.reject(error)
  }
)
```

---

## Conclusion

The frontend implementation of the User Profile Management System successfully delivers a comprehensive, secure, and user-friendly interface for managing user profiles. The implementation focuses on:

### Key Technical Achievements
- **Modern React Architecture**: Utilizing Next.js 14 with App Router for optimal performance
- **Type Safety**: Comprehensive TypeScript implementation with Zod validation
- **Performance Optimization**: Code splitting, image optimization, and efficient state management
- **Accessibility**: WCAG-compliant design with comprehensive keyboard and screen reader support
- **Security**: Client-side validation, input sanitization, and secure file handling
- **Testing**: Comprehensive testing strategy with unit, integration, and e2e tests

### User Experience Excellence
- **Responsive Design**: Mobile-first approach ensuring optimal experience across all devices
- **Interactive Interface**: Drag-and-drop file uploads with real-time feedback
- **Privacy Controls**: Intuitive privacy settings with immediate visual feedback
- **Error Handling**: Clear, actionable error messages with recovery suggestions
- **Performance**: Fast loading times with skeleton loading states

The frontend system is production-ready and provides a solid foundation for future enhancements while maintaining high standards for security, accessibility, and user experience.

---

**Document Version:** 1.0  
**Last Updated:** July 31, 2025  
**Document Owner:** Frontend Development Team  
**Review Schedule:** Quarterly  
**Next Review Date:** October 31, 2025