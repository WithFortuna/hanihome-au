# HaniHome AU Frontend Documentation

## Overview

HaniHome AU is a Korean-focused Australian rental platform built with modern web technologies. The frontend is developed using Next.js 15.4.4 with TypeScript and Tailwind CSS, providing a responsive and accessible user interface tailored for Korean users seeking rental properties in Australia.

## Table of Contents

1. [Project Setup and Configuration](#project-setup-and-configuration)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Docker Configuration](#docker-configuration)
5. [Development Workflow](#development-workflow)
6. [Components and UI](#components-and-ui)
7. [Styling and Theming](#styling-and-theming)
8. [Type Definitions](#type-definitions)
9. [Deployment Setup](#deployment-setup)
10. [Development Scripts](#development-scripts)
11. [Code Quality and Standards](#code-quality-and-standards)
12. [Future Development](#future-development)

## Project Setup and Configuration

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Next.js** | 15.4.4 | React framework with SSR/SSG capabilities |
| **React** | 19.1.0 | UI library |
| **TypeScript** | ^5 | Type-safe JavaScript |
| **Tailwind CSS** | ^4 | Utility-first CSS framework |
| **Radix UI** | ^1.2.3 | Headless UI components |
| **Lucide React** | ^0.533.0 | Icon library |
| **Node.js** | 20 (Alpine) | Runtime environment |

### Dependencies

#### Core Dependencies
```json
{
  "@radix-ui/react-slot": "^1.2.3",
  "class-variance-authority": "^0.7.1",
  "clsx": "^2.1.1",
  "lucide-react": "^0.533.0",
  "next": "15.4.4",
  "react": "19.1.0",
  "react-dom": "19.1.0",
  "tailwind-merge": "^3.3.1"
}
```

#### Development Dependencies
```json
{
  "@eslint/eslintrc": "^3",
  "@tailwindcss/postcss": "^4",
  "@types/node": "^20",
  "@types/react": "^19",
  "@types/react-dom": "^19",
  "eslint": "^9",
  "eslint-config-next": "15.4.4",
  "eslint-config-prettier": "^10.1.8",
  "eslint-plugin-prettier": "^5.5.3",
  "prettier": "^3.6.2",
  "tailwindcss": "^4",
  "typescript": "^5"
}
```

### Configuration Files

#### Next.js Configuration (`next.config.ts`)
```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'standalone',
};

export default nextConfig;
```

Key features:
- **Standalone output**: Optimized for Docker deployment
- Production-ready configuration for containerized environments

#### TypeScript Configuration (`tsconfig.json`)
```json
{
  "compilerOptions": {
    "target": "ES2017",
    "lib": ["dom", "dom.iterable", "esnext"],
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
    "plugins": [{"name": "next"}],
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

Key features:
- **Strict mode enabled**: Enhanced type checking
- **Path mapping**: `@/*` aliases for clean imports
- **Next.js plugin**: Integrated TypeScript support

## Project Structure

```
frontend/hanihome-au/
├── public/                          # Static assets
│   ├── file.svg
│   ├── globe.svg
│   ├── next.svg
│   ├── vercel.svg
│   └── window.svg
├── src/
│   ├── app/                         # Next.js App Router
│   │   ├── auth/
│   │   │   └── components/          # Authentication components
│   │   ├── dashboard/
│   │   │   └── components/          # Dashboard components
│   │   ├── profile/
│   │   │   └── components/          # User profile components
│   │   ├── property/
│   │   │   └── components/          # Property-related components
│   │   ├── search/
│   │   │   └── components/          # Search functionality components
│   │   ├── globals.css              # Global styles and CSS variables
│   │   ├── layout.tsx               # Root layout component
│   │   ├── page.tsx                 # Homepage component
│   │   └── favicon.ico
│   ├── components/                  # Shared components
│   │   ├── auth/                    # Authentication UI components
│   │   ├── forms/                   # Form components
│   │   ├── layout/                  # Layout components
│   │   ├── property/                # Property display components
│   │   ├── search/                  # Search UI components
│   │   └── ui/                      # Base UI components
│   │       └── button.tsx           # Button component
│   ├── hooks/                       # Custom React hooks
│   ├── lib/                         # Utility libraries
│   │   ├── api/                     # API client utilities
│   │   ├── auth/                    # Authentication utilities
│   │   ├── types/
│   │   │   └── index.ts             # TypeScript type definitions
│   │   ├── utils.ts                 # General utility functions
│   │   └── utils/                   # Additional utilities
├── Dockerfile                       # Docker configuration
├── README.md                        # Project README
├── eslint.config.mjs               # ESLint configuration
├── next-env.d.ts                   # Next.js type definitions
├── next.config.ts                  # Next.js configuration
├── package.json                    # Dependencies and scripts
├── postcss.config.mjs              # PostCSS configuration
└── tsconfig.json                   # TypeScript configuration
```

## Docker Configuration

### Multi-stage Dockerfile

The frontend uses a multi-stage Docker build optimized for production:

```dockerfile
# Multi-stage build for Next.js application
FROM node:20-alpine AS base

# Install dependencies only when needed
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY package*.json ./
RUN npm ci

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Copy built application
COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]
```

### Docker Compose Integration

The frontend is integrated into the full-stack development environment via `docker-compose.dev.yml`:

```yaml
frontend:
  build:
    context: ./frontend/hanihome-au
    dockerfile: Dockerfile
  container_name: hanihome-frontend-dev
  environment:
    NODE_ENV: development
    NEXT_PUBLIC_API_URL: http://localhost:8080
  ports:
    - "3000:3000"
  networks:
    - hanihome-network
  depends_on:
    backend:
      condition: service_healthy
  restart: unless-stopped
```

Key features:
- **Multi-stage optimization**: Minimal production image size
- **Security**: Non-root user execution
- **Environment integration**: Connected to backend and database services
- **Health checks**: Dependency management with backend service

## Development Workflow

### Available Scripts

```json
{
  "dev": "next dev --turbopack",
  "build": "next build",
  "start": "next start",
  "lint": "next lint",
  "lint:fix": "next lint --fix",
  "format": "prettier --write .",
  "format:check": "prettier --check .",
  "type-check": "tsc --noEmit",
  "clean": "rm -rf .next out",
  "test": "echo \"No tests specified\" && exit 0"
}
```

### Development Process

1. **Local Development**
   ```bash
   npm run dev          # Start development server with Turbopack
   ```

2. **Code Quality**
   ```bash
   npm run lint         # Check code quality
   npm run lint:fix     # Auto-fix linting issues
   npm run format       # Format code with Prettier
   npm run type-check   # TypeScript type checking
   ```

3. **Production Build**
   ```bash
   npm run build        # Build for production
   npm run start        # Start production server
   ```

4. **Docker Development**
   ```bash
   docker-compose -f docker-compose.dev.yml up --build
   ```

## Components and UI

### Implemented Components

#### 1. Button Component (`src/components/ui/button.tsx`)

A flexible, accessible button component built with Radix UI and class-variance-authority:

```typescript
const buttonVariants = cva(
  'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/90',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
        outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
        ghost: 'hover:bg-accent hover:text-accent-foreground',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      size: {
        default: 'h-10 px-4 py-2',
        sm: 'h-9 rounded-md px-3',
        lg: 'h-11 rounded-md px-8',
        icon: 'h-10 w-10',
      },
    }
  }
);
```

Features:
- **Accessibility**: ARIA compliant with focus management
- **Variants**: Multiple visual styles (default, outline, secondary, ghost, link, destructive)
- **Sizes**: Flexible sizing (sm, default, lg, icon)
- **TypeScript**: Full type safety with variant props

#### 2. Homepage Component (`src/app/page.tsx`)

The main landing page featuring:

- **Hero Section**: Brand introduction with Korean messaging
- **Search Interface**: Property search form with location, price, and bedroom filters
- **Features Section**: Three-column layout highlighting platform benefits
- **Call-to-Action**: User registration and login prompts
- **Footer**: Comprehensive site navigation and company information

Key features:
- **Bilingual Content**: Korean primary with English support
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Icon Integration**: Lucide React icons throughout
- **Brand Consistency**: Custom color scheme and typography

#### 3. Root Layout (`src/app/layout.tsx`)

Global application wrapper with:
- **Font Loading**: Geist Sans and Geist Mono integration
- **Metadata**: SEO-optimized page metadata
- **Global Styles**: CSS variables and theme integration

### Component Architecture

The component structure follows a hierarchical organization:

```
components/
├── ui/              # Base UI components (Button, Input, Card, etc.)
├── auth/            # Authentication-specific components
├── forms/           # Form components and field wrappers
├── layout/          # Layout components (Header, Footer, Sidebar)
├── property/        # Property display and management components
└── search/          # Search interface components
```

**Design Principles:**
- **Composition over Inheritance**: Radix UI primitives with custom styling
- **Accessibility First**: WCAG 2.1 AA compliance
- **TypeScript Integration**: Full type safety across all components
- **Theme Consistency**: Centralized design tokens

## Styling and Theming

### Tailwind CSS Integration

The project uses Tailwind CSS v4 with custom configuration:

#### PostCSS Configuration (`postcss.config.mjs`)
```javascript
const config = {
  plugins: ["@tailwindcss/postcss"],
};
```

#### Global Styles (`src/app/globals.css`)

**CSS Custom Properties:**
```css
:root {
  --background: #ffffff;
  --foreground: #171717;
  
  /* HaniHome AU Brand Colors */
  --primary: #0070f3;
  --primary-foreground: #ffffff;
  --secondary: #f3f4f6;
  --secondary-foreground: #374151;
  --accent: #fbbf24;
  --accent-foreground: #1f2937;
  --muted: #f9fafb;
  --muted-foreground: #6b7280;
  --border: #e5e7eb;
  --input: #f3f4f6;
  --ring: #3b82f6;
  --destructive: #ef4444;
  --destructive-foreground: #ffffff;
  
  /* Property related colors */
  --property-available: #10b981;
  --property-pending: #f59e0b; 
  --property-rented: #6b7280;
}
```

**Theme Integration:**
```css
@theme inline {
  --color-background: var(--background);
  --color-foreground: var(--foreground);
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  /* ... additional theme mappings */
  --font-sans: var(--font-geist-sans);
  --font-mono: var(--font-geist-mono);
}
```

**Dark Mode Support:**
```css
@media (prefers-color-scheme: dark) {
  :root {
    --background: #0a0a0a;
    --foreground: #ededed;
  }
}
```

### Design System

#### Color Palette
- **Primary**: Blue (#0070f3) - Primary actions and branding
- **Secondary**: Gray (#f3f4f6) - Secondary elements
- **Accent**: Amber (#fbbf24) - Highlights and notifications
- **Property Status**: Green (available), Amber (pending), Gray (rented)

#### Typography
- **Primary Font**: Geist Sans - Modern, readable sans-serif
- **Monospace Font**: Geist Mono - Code and technical content
- **Hierarchy**: Responsive text scaling with consistent line heights

#### Spacing and Layout
- **Container**: Max-width responsive containers (max-w-6xl, max-w-4xl)
- **Grid System**: CSS Grid and Flexbox for responsive layouts
- **Spacing**: Consistent padding and margin scale (4px base unit)

## Type Definitions

### Core Types (`src/lib/types/index.ts`)

#### User Management
```typescript
export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  avatar?: string;
  phone?: string;
  preferredLocations?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export enum UserRole {
  TENANT = 'TENANT',
  LANDLORD = 'LANDLORD', 
  AGENT = 'AGENT',
  ADMIN = 'ADMIN',
}
```

#### Property Management
```typescript
export interface Property {
  id: string;
  title: string;
  description: string;
  address: string;
  suburb: string;
  state: string;
  postcode: string;
  coordinates: {
    lat: number;
    lng: number;
  };
  propertyType: PropertyType;
  price: number;
  bedrooms: number;
  bathrooms: number;
  parking: number;
  availableDate: Date;
  features: PropertyFeature[];
  images: PropertyImage[];
  status: PropertyStatus;
  landlordId: string;
  agentId?: string;
  createdAt: Date;
  updatedAt: Date;
}

export enum PropertyType {
  APARTMENT = 'APARTMENT',
  HOUSE = 'HOUSE',
  TOWNHOUSE = 'TOWNHOUSE',
  STUDIO = 'STUDIO',
  SHARED_ROOM = 'SHARED_ROOM',
}

export enum PropertyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE', 
  RENTED = 'RENTED',
  PENDING = 'PENDING',
}
```

#### Search and Filtering
```typescript
export interface SearchFilters {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  propertyType?: PropertyType[];
  bedrooms?: number;
  bathrooms?: number;
  features?: string[];
  sortBy?: SortOption;
}

export enum SortOption {
  PRICE_LOW = 'PRICE_LOW',
  PRICE_HIGH = 'PRICE_HIGH', 
  DATE_NEW = 'DATE_NEW',
  DATE_OLD = 'DATE_OLD',
  DISTANCE = 'DISTANCE',
}
```

#### API Integration
```typescript
export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}
```

### Utility Functions (`src/lib/utils.ts`)

```typescript
// Class name merging utility
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Australian currency formatting
export function formatCurrency(amount: number, currency = 'AUD'): string {
  return new Intl.NumberFormat('en-AU', {
    style: 'currency',
    currency,
  }).format(amount);
}

// Date formatting for Australian locale
export function formatDate(date: Date | string, format = 'short'): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (format === 'short') {
    return dateObj.toLocaleDateString('en-AU');
  }
  
  return dateObj.toLocaleDateString('en-AU', {
    year: 'numeric',
    month: 'long', 
    day: 'numeric',
  });
}

// Unique ID generation
export function generateId(): string {
  return Math.random().toString(36).substring(2) + Date.now().toString(36);
}
```

## Code Quality and Standards

### ESLint Configuration (`eslint.config.mjs`)

```javascript
const eslintConfig = [
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  ...compat.extends("prettier"),
  {
    plugins: {
      ...compat.plugins("prettier"),
    },
    rules: {
      "prettier/prettier": "error",
      "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
      "@typescript-eslint/no-explicit-any": "warn",
    },
  },
];
```

**Quality Standards:**
- **Next.js Best Practices**: Core web vitals and TypeScript integration
- **Prettier Integration**: Automated code formatting
- **TypeScript Rules**: Unused variables detection and explicit any warnings
- **Accessibility**: WCAG 2.1 AA compliance where applicable

### Development Guidelines

1. **Code Organization**
   - Components organized by feature and hierarchy
   - Consistent import/export patterns
   - Clear separation of concerns

2. **TypeScript Usage**
   - Strict mode enabled
   - Explicit type annotations for complex objects
   - Generic types for reusable components

3. **Performance Optimization**
   - Next.js Image component for optimized loading
   - Lazy loading for non-critical components
   - Efficient bundle splitting

4. **Accessibility**
   - Semantic HTML structure
   - ARIA labels and roles
   - Keyboard navigation support
   - Color contrast compliance

## Deployment Setup

### Production Configuration

The frontend is configured for containerized deployment with:

1. **Standalone Output**: Next.js standalone mode for minimal Docker images
2. **Multi-stage Build**: Optimized build process with dependency caching
3. **Security**: Non-root user execution in production containers
4. **Environment Variables**: Configurable API endpoints and settings

### Environment Variables

```bash
NODE_ENV=production                    # Production mode
NEXT_PUBLIC_API_URL=http://backend:8080    # Backend API endpoint
NEXT_TELEMETRY_DISABLED=1             # Disable Next.js telemetry
PORT=3000                             # Application port
HOSTNAME=0.0.0.0                      # Bind to all interfaces
```

### Deployment Process

1. **Build Stage**
   ```bash
   npm ci                    # Install dependencies
   npm run build            # Build application
   ```

2. **Production Stage**
   ```bash
   docker build -t hanihome-au-frontend .
   docker run -p 3000:3000 hanihome-au-frontend
   ```

3. **Full Stack Deployment**
   ```bash
   docker-compose -f docker-compose.dev.yml up --build
   ```

## Future Development

### Planned Components

Based on the existing project structure, the following components are planned for implementation:

#### Authentication System (`src/app/auth/` & `src/components/auth/`)
- Login/Register forms
- Password reset functionality
- OAuth integration (Google, Kakao)
- Session management

#### Property Management (`src/app/property/` & `src/components/property/`)
- Property listing cards
- Detailed property views
- Property creation/editing forms
- Image upload and gallery
- Map integration

#### Search Functionality (`src/app/search/` & `src/components/search/`)
- Advanced search filters
- Map-based search
- Search results display
- Saved searches

#### User Dashboard (`src/app/dashboard/` & `src/components/dashboard/`)
- User dashboard layout
- Property management interface
- Booking management
- Analytics and insights

#### Profile Management (`src/app/profile/` & `src/components/profile/`)
- User profile editing
- Preferences management
- Account settings
- Communication preferences

### Technical Enhancements

1. **State Management**: Redux Toolkit or Zustand integration
2. **API Integration**: React Query for server state management
3. **Testing**: Jest and React Testing Library setup
4. **Internationalization**: Next.js i18n for Korean/English support
5. **PWA Features**: Service worker and offline functionality
6. **Performance**: Image optimization and lazy loading
7. **SEO**: Enhanced metadata and structured data

### Development Infrastructure

1. **Testing Strategy**
   - Unit tests for utility functions
   - Component testing with React Testing Library
   - E2E testing with Playwright
   - Visual regression testing

2. **CI/CD Pipeline**
   - GitHub Actions for automated testing
   - Docker image building and publishing
   - Automated deployment to staging/production

3. **Monitoring and Analytics**
   - Error tracking with Sentry
   - Performance monitoring
   - User analytics integration

## Conclusion

The HaniHome AU frontend provides a solid foundation for a Korean-focused Australian rental platform. Built with modern technologies and following best practices, the application is designed for scalability, maintainability, and excellent user experience.

The current implementation includes:
- ✅ Modern Next.js 15.4.4 setup with TypeScript
- ✅ Responsive, accessible UI components
- ✅ Docker containerization for production deployment
- ✅ Comprehensive type definitions for data models
- ✅ Code quality tools and linting configuration
- ✅ Korean-English bilingual content structure

The architecture supports future expansion with well-organized component hierarchies and a clear separation of concerns, making it ready for additional features and team collaboration.

---

**Generated**: 2025-07-30  
**Version**: 1.0.0  
**Framework**: Next.js 15.4.4  
**Documentation Path**: `/frontend/document/frontend-documentation.md`