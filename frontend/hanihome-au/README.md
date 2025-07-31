# HaniHome Australia - Frontend 🎨

> Next.js-based web application for Australian real estate management platform

[![Next.js](https://img.shields.io/badge/Next.js-15.4.4-black)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-38B2AC)](https://tailwindcss.com/)

## 📖 Overview

The frontend of HaniHome Australia is a modern, responsive web application built with Next.js 15, featuring property search, user authentication, and geographic mapping capabilities.

### ✨ Key Features

- **🏠 Property Management**: Browse, search, and manage property listings
- **🗺️ Interactive Maps**: Google Maps integration with clustering and property markers
- **🔐 Authentication**: NextAuth.js v5 with OAuth2 support (Google, Kakao)
- **👤 User Profiles**: Profile management and preferences
- **📱 Responsive Design**: Mobile-first approach with Tailwind CSS
- **⚡ Performance**: Optimized with Next.js App Router and React 19

## 🛠️ Tech Stack

- **Framework**: Next.js 15.4.4 (App Router)
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 4
- **UI Components**: Radix UI primitives
- **Authentication**: NextAuth.js v5
- **Maps**: Google Maps JavaScript API
- **State Management**: React Context + Hooks
- **HTTP Client**: Axios
- **Icons**: Lucide React

## 🏗️ Project Structure

```
src/
├── app/                    # Next.js App Router
│   ├── (auth)/            # Authentication routes
│   ├── api/               # API routes
│   ├── dashboard/         # Dashboard pages
│   ├── profile/           # User profile pages
│   ├── property/          # Property pages
│   └── search/            # Search pages
├── components/            # Reusable components
│   ├── auth/             # Authentication components
│   ├── forms/            # Form components
│   ├── layout/           # Layout components
│   ├── maps/             # Map components
│   ├── property/         # Property components
│   ├── search/           # Search components
│   └── ui/               # Base UI components
├── hooks/                # Custom React hooks
├── lib/                  # Utilities and configurations
│   ├── api/             # API client setup
│   ├── auth/            # Auth configuration
│   ├── maps/            # Maps utilities
│   ├── types/           # TypeScript definitions
│   └── utils/           # General utilities
└── middleware.ts        # Next.js middleware
```

## 🚀 Getting Started

### Prerequisites

- **Node.js** 18.0.0 or later
- **npm** 9.0.0 or later
- **Google Maps API Key** (for maps functionality)

### Installation

1. **Navigate to frontend directory**:
   ```bash
   cd frontend/hanihome-au
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Environment setup**:
   ```bash
   cp .env.example .env.local
   ```

4. **Configure environment variables** in `.env.local`:
   ```env
   # NextAuth Configuration
   NEXTAUTH_URL=http://localhost:3000
   NEXTAUTH_SECRET=your-nextauth-secret
   
   # Backend API
   NEXT_PUBLIC_API_URL=http://localhost:8080
   
   # Google Maps
   NEXT_PUBLIC_GOOGLE_MAPS_API_KEY=your-google-maps-api-key
   
   # OAuth Providers
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   KAKAO_CLIENT_ID=your-kakao-client-id
   KAKAO_CLIENT_SECRET=your-kakao-client-secret
   ```

### Development

1. **Start the development server**:
   ```bash
   npm run dev
   ```

2. **Open your browser**: http://localhost:3000

3. **Development features**:
   - Hot reloading
   - TypeScript checking
   - ESLint integration
   - Prettier formatting

## 📜 Available Scripts

```bash
# Development
npm run dev              # Start development server with Turbopack
npm run build           # Build for production
npm run start           # Start production server

# Code Quality
npm run lint            # Run ESLint
npm run lint:fix        # Fix ESLint issues
npm run format          # Format code with Prettier
npm run format:check    # Check code formatting
npm run type-check      # Run TypeScript type checking

# Maintenance
npm run clean           # Clean build artifacts
```

## 🗺️ Key Components

### Authentication Components
- `LoginForm`: User login interface
- `SessionProvider`: NextAuth session management
- `RoleGuard`: Route protection based on user roles
- `UserProfile`: User profile display and editing

### Map Components
- `GoogleMap`: Main map component with clustering
- `PropertyMap`: Property-specific map with markers
- `AddressSearch`: Address autocomplete functionality
- `PropertyMarker`: Custom property markers

### Property Components
- `PropertyCard`: Property listing card
- `PropertyDetails`: Detailed property view
- `PropertyForm`: Property creation/editing form
- `PropertyFilters`: Search and filter interface

## 🎨 Styling Guidelines

### Tailwind CSS Configuration
- **Mobile-first** responsive design
- **Dark mode** support (configured)
- **Custom color palette** for brand consistency
- **Component variants** using `class-variance-authority`

### Design System
- **Colors**: Primary (blue), secondary (green), accent (orange)
- **Typography**: Inter font family
- **Spacing**: 8px base unit
- **Border radius**: 8px default

## 🔧 Configuration

### Next.js Configuration
```typescript
// next.config.ts
const nextConfig = {
  experimental: {
    turbo: {
      rules: {
        '*.svg': {
          loaders: ['@svgr/webpack'],
          as: '*.js',
        },
      },
    },
  },
  images: {
    domains: ['maps.googleapis.com', 'lh3.googleusercontent.com'],
  },
}
```

### TypeScript Configuration
- Strict mode enabled
- Path aliases configured (`@/` → `src/`)
- Custom type definitions in `lib/types/`

## 🔐 Authentication Flow

1. **Login Options**: Email/password, Google OAuth, Kakao OAuth
2. **Session Management**: NextAuth.js handles JWT tokens
3. **Protected Routes**: Middleware checks authentication
4. **Role-based Access**: Different permissions for users/admins

## 🗺️ Maps Integration

### Google Maps Features
- **Property clustering** for performance
- **Custom markers** for different property types
- **Address autocomplete** with Places API
- **Geolocation services** for user location
- **Distance calculations** between properties

### Configuration
```typescript
// lib/maps/config.ts
export const GOOGLE_MAPS_CONFIG = {
  apiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY,
  libraries: ['places', 'geometry'],
  region: 'AU',
  language: 'en',
}
```

## 📊 Performance Optimization

### Build Optimization
- **Turbopack** for faster builds
- **Bundle analysis** with Next.js analyzer
- **Image optimization** with Next.js Image component
- **Font optimization** with Next.js Font

### Runtime Performance
- **Code splitting** by route and component
- **Lazy loading** for maps and heavy components
- **Memoization** for expensive calculations
- **Virtualization** for large property lists

## 🧪 Testing

### Testing Setup
```bash
# Install testing dependencies (when configured)
npm install --save-dev jest @testing-library/react @testing-library/jest-dom

# Run tests
npm run test
npm run test:watch
npm run test:coverage
```

### Testing Strategy
- **Unit tests** for utilities and hooks
- **Component tests** with React Testing Library
- **Integration tests** for critical user flows
- **E2E tests** with Playwright (planned)

## 🚀 Deployment

### Build Process
```bash
# Production build
npm run build

# Verify build
npm run start
```

### Deployment Options

#### Vercel (Recommended)
```bash
# Connect to Vercel
npx vercel

# Deploy
npx vercel --prod
```

#### Docker
```dockerfile
# Use official Node.js image
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy and install dependencies
COPY package*.json ./
RUN npm ci --only=production

# Copy source code
COPY . .

# Build application
RUN npm run build

# Expose port and start
EXPOSE 3000
CMD ["npm", "start"]
```

## 🔍 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `NEXTAUTH_URL` | Base URL for NextAuth | ✅ |
| `NEXTAUTH_SECRET` | NextAuth encryption secret | ✅ |
| `NEXT_PUBLIC_API_URL` | Backend API URL | ✅ |
| `NEXT_PUBLIC_GOOGLE_MAPS_API_KEY` | Google Maps API key | ✅ |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | ⚠️ |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret | ⚠️ |
| `KAKAO_CLIENT_ID` | Kakao OAuth client ID | ⚠️ |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth client secret | ⚠️ |

## 🐛 Troubleshooting

### Common Issues

**Maps not loading**:
- Check Google Maps API key
- Verify API key restrictions
- Ensure required APIs are enabled

**Authentication issues**:
- Verify OAuth provider configuration
- Check NextAuth secret configuration
- Ensure correct redirect URLs

**Build failures**:
- Clear `.next` directory: `npm run clean`
- Reinstall dependencies: `rm -rf node_modules && npm install`
- Check TypeScript errors: `npm run type-check`

## 🤝 Contributing

1. Follow the established code style
2. Use TypeScript strictly
3. Write meaningful commit messages
4. Add tests for new features
5. Update documentation as needed

### Code Style
- Use Prettier for formatting
- Follow ESLint rules
- Use meaningful variable names
- Add JSDoc comments for complex functions

---

**Part of the [HaniHome Australia](../../README.md) monorepo**
