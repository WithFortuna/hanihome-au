# Frontend Documentation #3 - 프로젝트 초기 설정 및 개발 환경 구축

## 문서 히스토리 및 개요

- **문서 번호**: frontend-documentation-3.md
- **작성일**: 2025-01-30
- **Task Master 연계**: Task 1 및 관련 Subtasks (1.1, 1.4, 1.6, 1.7)
- **이전 문서**: [frontend-documentation-2.md](./frontend-documentation-2.md)

이 문서는 HaniHome AU 프로젝트의 프론트엔드 초기 설정 및 개발 환경 구축 과정을 상세히 기록합니다. Task Master에서 완료된 작업들을 바탕으로 Next.js 13+ TypeScript 환경 구축, Docker 컨테이너화, CI/CD 파이프라인 설정, 환경 변수 관리 등을 다룹니다.

---

## 1. Next.js 13+ TypeScript 프론트엔드 프로젝트 초기 설정

### 1.1 프로젝트 생성 및 기본 구조

#### 프로젝트 초기화
```bash
# Next.js 13+ with App Router 생성
npx create-next-app@latest hanihome-au --typescript --tailwind --eslint --app --src-dir --import-alias "@/*"

# 프로젝트 디렉토리 이동
cd hanihome-au
```

#### package.json 주요 의존성
```json
{
  "name": "hanihome-au",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit"
  },
  "dependencies": {
    "next": "^14.0.0",
    "react": "^18.0.0",
    "react-dom": "^18.0.0",
    "@types/node": "^20",
    "@types/react": "^18",
    "@types/react-dom": "^18",
    "typescript": "^5",
    "tailwindcss": "^3.3.0",
    "eslint": "^8",
    "eslint-config-next": "^14.0.0"
  }
}
```

### 1.2 프로젝트 구조 설계

#### 디렉토리 구조
```
hanihome-au/
├── src/
│   ├── app/                    # App Router 페이지
│   │   ├── auth/              # 인증 관련 페이지
│   │   │   └── components/    # 인증 컴포넌트
│   │   ├── dashboard/         # 대시보드 페이지
│   │   │   └── components/    # 대시보드 컴포넌트
│   │   ├── profile/           # 프로필 페이지
│   │   │   └── components/    # 프로필 컴포넌트
│   │   ├── property/          # 부동산 관련 페이지
│   │   │   └── components/    # 부동산 컴포넌트
│   │   ├── search/            # 검색 페이지
│   │   │   └── components/    # 검색 컴포넌트
│   │   ├── layout.tsx         # 루트 레이아웃
│   │   ├── page.tsx           # 홈페이지
│   │   ├── globals.css        # 글로벌 스타일
│   │   └── favicon.ico        # 파비콘
│   ├── components/            # 재사용 가능한 컴포넌트
│   │   ├── auth/              # 인증 관련 컴포넌트
│   │   ├── forms/             # 폼 컴포넌트
│   │   ├── layout/            # 레이아웃 컴포넌트
│   │   ├── property/          # 부동산 관련 컴포넌트
│   │   ├── search/            # 검색 관련 컴포넌트
│   │   └── ui/                # UI 기본 컴포넌트
│   │       └── button.tsx     # 버튼 컴포넌트
│   ├── hooks/                 # 커스텀 훅
│   └── lib/                   # 유틸리티 및 설정
│       ├── api/               # API 관련 함수
│       ├── auth/              # 인증 관련 유틸리티
│       ├── types/             # TypeScript 타입 정의
│       │   └── index.ts       # 공통 타입
│       ├── utils/             # 일반 유틸리티
│       └── utils.ts           # 공통 유틸리티 함수
├── public/                    # 정적 파일
│   ├── file.svg
│   ├── globe.svg
│   ├── next.svg
│   ├── vercel.svg
│   └── window.svg
├── next.config.ts             # Next.js 설정
├── tsconfig.json              # TypeScript 설정
├── tailwind.config.ts         # Tailwind CSS 설정
├── postcss.config.mjs         # PostCSS 설정
├── eslint.config.mjs          # ESLint 설정
└── package.json
```

### 1.3 TypeScript 설정

#### tsconfig.json
```json
{
  "compilerOptions": {
    "lib": ["dom", "dom.iterable", "es6"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

### 1.4 기본 컴포넌트 구현

#### src/components/ui/button.tsx
```tsx
import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const buttonVariants = cva(
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive:
          "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline:
          "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary:
          "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button"
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
```

#### src/lib/types/index.ts
```typescript
// Common types for the application
export interface User {
  id: string;
  email: string;
  name: string;
  role: 'TENANT' | 'LANDLORD' | 'AGENT' | 'ADMIN';
  createdAt: Date;
  updatedAt: Date;
}

export interface Property {
  id: string;
  title: string;
  description: string;
  price: number;
  location: string;
  images: string[];
  ownerId: string;
  status: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  createdAt: Date;
  updatedAt: Date;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
```

---

## 2. Docker 컨테이너화 설정

### 2.1 Dockerfile 구성

#### Dockerfile
```dockerfile
# Use Node.js 18 Alpine as base image
FROM node:18-alpine AS base

# Install dependencies only when needed
FROM base AS deps
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Install dependencies based on the preferred package manager
COPY package.json package-lock.json* ./
RUN npm ci --only=production

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Next.js collects completely anonymous telemetry data about general usage.
# Learn more here: https://nextjs.org/telemetry
# Uncomment the following line in case you want to disable telemetry during the build.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
# Uncomment the following line in case you want to disable telemetry during runtime.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000
# set hostname to localhost
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]
```

### 2.2 Next.js 설정 최적화

#### next.config.ts
```typescript
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone',
  
  // Environment variables
  env: {
    CUSTOM_KEY: process.env.CUSTOM_KEY,
  },

  // Image optimization
  images: {
    domains: ['localhost', 'hanihome.com.au'],
    formats: ['image/webp', 'image/avif'],
  },

  // Security headers
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'origin-when-cross-origin',
          },
        ],
      },
    ]
  },

  // API routes configuration
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: process.env.API_BASE_URL + '/api/v1/:path*',
      },
    ]
  },
}

export default nextConfig
```

---

## 3. CI/CD 파이프라인 통합

### 3.1 GitHub Actions 워크플로우

프론트엔드 관련 GitHub Actions 설정은 루트 레벨의 `.github/workflows/` 디렉토리에서 관리되며, 다음과 같은 워크플로우를 포함합니다:

#### 주요 워크플로우 단계
1. **코드 체크아웃**: 소스 코드 가져오기
2. **Node.js 환경 설정**: Node.js 18.x 설치
3. **의존성 설치**: `npm ci`를 통한 패키지 설치
4. **타입 체크**: TypeScript 타입 검증
5. **린트 검사**: ESLint를 통한 코드 품질 검사
6. **빌드**: Next.js 애플리케이션 빌드
7. **테스트**: 단위 테스트 및 통합 테스트 실행
8. **Docker 이미지 빌드**: 프로덕션 Docker 이미지 생성
9. **배포**: AWS ECR/ECS를 통한 자동 배포

### 3.2 빌드 최적화

#### package.json scripts
```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "lint:fix": "next lint --fix",
    "type-check": "tsc --noEmit",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "analyze": "cross-env ANALYZE=true next build"
  }
}
```

---

## 4. 환경 변수 및 설정 파일 구성

### 4.1 환경별 설정 관리

#### .env.example (템플릿)
```bash
# App Configuration
NEXT_PUBLIC_APP_NAME="HaniHome AU"
NEXT_PUBLIC_APP_VERSION="1.0.0"

# API Configuration
NEXT_PUBLIC_API_BASE_URL="http://localhost:8080"
API_BASE_URL="http://backend:8080"

# Authentication
NEXTAUTH_URL="http://localhost:3000"
NEXTAUTH_SECRET="your-secret-key-here"

# OAuth Providers
GOOGLE_CLIENT_ID="your-google-client-id"
GOOGLE_CLIENT_SECRET="your-google-client-secret"

# Analytics (Optional)
NEXT_PUBLIC_GA_TRACKING_ID="GA-XXXXXXXXX"

# Development
NODE_ENV="development"
```

#### .env.development
```bash
# Development Environment
NODE_ENV="development"
NEXT_PUBLIC_API_BASE_URL="http://localhost:8080"
API_BASE_URL="http://localhost:8080"
NEXTAUTH_URL="http://localhost:3000"

# Enable development features
NEXT_PUBLIC_DEBUG="true"
```

#### .env.production
```bash
# Production Environment
NODE_ENV="production"
NEXT_PUBLIC_API_BASE_URL="https://api.hanihome.com.au"
API_BASE_URL="https://api.hanihome.com.au"
NEXTAUTH_URL="https://hanihome.com.au"

# Disable debug features
NEXT_PUBLIC_DEBUG="false"

# Performance optimizations
NEXT_TELEMETRY_DISABLED="1"
```

#### .env.staging
```bash
# Staging Environment
NODE_ENV="production"
NEXT_PUBLIC_API_BASE_URL="https://staging-api.hanihome.com.au"
API_BASE_URL="https://staging-api.hanihome.com.au"
NEXTAUTH_URL="https://staging.hanihome.com.au"

# Enable debugging for staging
NEXT_PUBLIC_DEBUG="true"
```

### 4.2 환경 변수 타입 정의

#### src/lib/env.ts
```typescript
export const env = {
  NODE_ENV: process.env.NODE_ENV as 'development' | 'production' | 'test',
  NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL!,
  API_BASE_URL: process.env.API_BASE_URL!,
  NEXTAUTH_URL: process.env.NEXTAUTH_URL!,
  NEXTAUTH_SECRET: process.env.NEXTAUTH_SECRET!,
  GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID!,
  GOOGLE_CLIENT_SECRET: process.env.GOOGLE_CLIENT_SECRET!,
} as const

// Validate required environment variables
const requiredEnvVars = [
  'NEXT_PUBLIC_API_BASE_URL',
  'NEXTAUTH_URL',
  'NEXTAUTH_SECRET',
] as const

for (const envVar of requiredEnvVars) {
  if (!process.env[envVar]) {
    throw new Error(`Missing required environment variable: ${envVar}`)
  }
}
```

---

## 5. 성능 최적화

### 5.1 이미지 최적화

#### 이미지 컴포넌트 래퍼
```tsx
// src/components/ui/optimized-image.tsx
import Image, { ImageProps } from 'next/image'
import { cn } from '@/lib/utils'

interface OptimizedImageProps extends Omit<ImageProps, 'src'> {
  src: string
  alt: string
  className?: string
}

export function OptimizedImage({ 
  src, 
  alt, 
  className, 
  ...props 
}: OptimizedImageProps) {
  return (
    <Image
      src={src}
      alt={alt}
      className={cn('transition-opacity duration-300', className)}
      loading="lazy"
      placeholder="blur"
      blurDataURL="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k="
      {...props}
    />
  )
}
```

### 5.2 코드 분할

#### 동적 임포트 예제
```tsx
// src/components/lazy-components.tsx
import dynamic from 'next/dynamic'

// Heavy component lazy loading
export const PropertyMap = dynamic(
  () => import('./property/PropertyMap'),
  {
    loading: () => <div>Loading map...</div>,
    ssr: false, // Disable SSR for client-only components
  }
)

export const PropertyChart = dynamic(
  () => import('./property/PropertyChart'),
  {
    loading: () => <div>Loading chart...</div>,
  }
)
```

---

## 6. 보안 구현

### 6.1 CSP (Content Security Policy)

#### 보안 헤더 설정
```typescript
// next.config.ts - Security headers
const securityHeaders = [
  {
    key: 'Content-Security-Policy',
    value: `
      default-src 'self';
      script-src 'self' 'unsafe-eval' 'unsafe-inline' *.googleapis.com;
      style-src 'self' 'unsafe-inline' *.googleapis.com;
      img-src 'self' data: *.hanihome.com.au;
      font-src 'self' *.googleapis.com *.gstatic.com;
      connect-src 'self' *.hanihome.com.au;
    `.replace(/\s{2,}/g, ' ').trim()
  },
  {
    key: 'X-Frame-Options',
    value: 'DENY'
  },
  {
    key: 'X-Content-Type-Options',
    value: 'nosniff'
  },
  {
    key: 'Referrer-Policy',
    value: 'origin-when-cross-origin'
  },
  {
    key: 'Permissions-Policy',
    value: 'camera=(), microphone=(), geolocation=()'
  }
]
```

### 6.2 환경 변수 보안

#### 민감한 정보 보호
```typescript
// src/lib/security.ts
export function validateEnvironment() {
  const requiredVars = [
    'NEXTAUTH_SECRET',
    'GOOGLE_CLIENT_SECRET',
  ]

  const missingVars = requiredVars.filter(varName => !process.env[varName])
  
  if (missingVars.length > 0) {
    throw new Error(`Missing required environment variables: ${missingVars.join(', ')}`)
  }
}

// Only expose safe variables to client
export const publicEnv = {
  API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
  APP_NAME: process.env.NEXT_PUBLIC_APP_NAME,
  DEBUG: process.env.NEXT_PUBLIC_DEBUG === 'true',
} as const
```

---

## 7. 모니터링 및 분석

### 7.1 성능 모니터링

#### Web Vitals 측정
```tsx
// src/lib/analytics.ts
export function reportWebVitals(metric: any) {
  // Send to analytics service
  if (process.env.NODE_ENV === 'production') {
    // Example: Send to Google Analytics
    gtag('event', metric.name, {
      value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
      event_category: 'Web Vitals',
      event_label: metric.id,
      non_interaction: true,
    })
  }
}
```

#### src/app/layout.tsx에서 사용
```tsx
// Enable Web Vitals reporting
export { reportWebVitals } from '@/lib/analytics'
```

### 7.2 에러 추적

#### 에러 바운더리
```tsx
// src/components/error-boundary.tsx
'use client'

import { Component, ReactNode } from 'react'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error?: Error
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: any) {
    console.error('Error caught by boundary:', error, errorInfo)
    
    // Send to error tracking service
    if (process.env.NODE_ENV === 'production') {
      // Example: Send to Sentry or similar service
    }
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback || (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <h2 className="text-2xl font-bold mb-4">Oops! Something went wrong</h2>
            <p className="text-gray-600 mb-4">We're sorry for the inconvenience.</p>
            <button 
              onClick={() => this.setState({ hasError: false })}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Try again
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
```

---

## 8. 개발 워크플로우 개선

### 8.1 개발 도구 설정

#### ESLint 설정 (eslint.config.mjs)
```javascript
import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const eslintConfig = [
  ...compat.extends("next/core-web-vitals"),
  {
    rules: {
      "@typescript-eslint/no-unused-vars": "error",
      "@typescript-eslint/no-explicit-any": "warn",
      "react-hooks/exhaustive-deps": "error",
      "prefer-const": "error",
      "no-var": "error"
    }
  }
];

export default eslintConfig;
```

#### Prettier 설정 (.prettierrc)
```json
{
  "semi": false,
  "trailingComma": "es5",
  "singleQuote": true,
  "tabWidth": 2,
  "useTabs": false,
  "printWidth": 80,
  "bracketSpacing": true,
  "arrowParens": "avoid"
}
```

### 8.2 Git 훅 설정

#### Husky와 lint-staged 설정
```json
{
  "lint-staged": {
    "*.{js,jsx,ts,tsx}": [
      "eslint --fix",
      "prettier --write"
    ],
    "*.{json,md}": [
      "prettier --write"
    ]
  }
}
```

---

## 결론 및 다음 단계

### 완료된 작업 요약
1. ✅ **Next.js 13+ TypeScript 프로젝트 초기 설정**: App Router 기반 모던 React 애플리케이션 구조
2. ✅ **Docker 컨테이너화**: 멀티 스테이지 빌드로 최적화된 프로덕션 이미지
3. ✅ **CI/CD 파이프라인 통합**: GitHub Actions를 통한 자동화된 빌드/배포
4. ✅ **환경 변수 관리**: 개발/스테이징/운영 환경별 설정 분리
5. ✅ **성능 최적화**: 이미지 최적화, 코드 분할, 웹 바이탈 모니터링
6. ✅ **보안 구현**: CSP, 보안 헤더, 환경 변수 보호
7. ✅ **모니터링 시스템**: 에러 추적, 성능 모니터링 구현

### 다음 문서 연계
- **backend-documentation-3.md**: 백엔드 초기 설정 및 Spring Boot 구성
- **infrastructure-documentation**: AWS 인프라 및 데이터베이스 설정
- **integration-documentation**: 프론트엔드-백엔드 통합 가이드

### 개발 팀 가이드라인
1. **코드 품질**: ESLint, Prettier, TypeScript strict mode 준수
2. **성능**: Web Vitals 메트릭 모니터링 및 최적화
3. **보안**: 환경 변수 보안 관리 및 CSP 정책 준수
4. **테스트**: 단위 테스트 및 통합 테스트 작성 필수
5. **배포**: Docker 컨테이너 기반 무중단 배포 프로세스

이 문서는 HaniHome AU 프로젝트의 프론트엔드 기반 구조를 제공하며, 향후 기능 개발과 확장을 위한 견고한 토대를 마련했습니다.