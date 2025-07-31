# HaniHome AU Frontend Documentation - Part 2
## Task 1.5-1.7 Implementation Details

### Document History
- **Part 1**: Initial Next.js setup, basic configuration
- **Part 2**: Environment configuration, CI/CD integration, production optimization

---

## Table of Contents

1. [Environment Configuration](#environment-configuration)
2. [CI/CD Pipeline Integration](#cicd-pipeline-integration)
3. [Docker Configuration](#docker-configuration)
4. [Production Optimization](#production-optimization)
5. [Security Implementation](#security-implementation)
6. [Monitoring and Analytics](#monitoring-and-analytics)

---

## Environment Configuration

### Environment Files Structure

The frontend now supports multiple environment configurations:

```
frontend/hanihome-au/
├── .env.local.example     # Template for local development
├── .env.development       # Development environment
├── .env.staging          # Staging environment
└── .env.production       # Production environment
```

### Key Environment Variables

#### API Integration
```bash
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_API_TIMEOUT=30000
```

#### Application Settings
```bash
# Application Configuration
NEXT_PUBLIC_APP_NAME=HaniHome Australia
NEXT_PUBLIC_APP_URL=http://localhost:3000
NEXT_PUBLIC_APP_VERSION=1.0.0
```

#### Authentication
```bash
# OAuth Configuration
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your-google-client-id
NEXT_PUBLIC_KAKAO_CLIENT_ID=your-kakao-client-id

# NextAuth.js Configuration
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-nextauth-secret
NEXTAUTH_JWT_SECRET=your-jwt-secret
```

#### External Services
```bash
# Google Analytics
NEXT_PUBLIC_GA_ID=GA-TRACKING-ID

# Sentry Error Tracking
SENTRY_DSN=your-sentry-dsn
NEXT_PUBLIC_SENTRY_DSN=your-sentry-dsn

# Google Maps
NEXT_PUBLIC_GOOGLE_MAPS_API_KEY=your-google-maps-api-key
```

#### Feature Flags
```bash
# Development Features
NEXT_PUBLIC_DEBUG_MODE=true
NEXT_PUBLIC_MOCK_API=false
NEXT_PUBLIC_ENABLE_ANALYTICS=false
NEXT_PUBLIC_ENABLE_ERROR_TRACKING=false
```

### Environment-Specific Configurations

#### Development Environment
- **Debug Mode**: Enabled for detailed logging
- **Mock API**: Optional for offline development
- **Analytics**: Disabled to avoid test data
- **CORS**: Relaxed for local development
- **HTTPS**: Not enforced

#### Staging Environment
- **Debug Mode**: Disabled
- **Analytics**: Enabled with staging tracking ID
- **Error Tracking**: Enabled for testing
- **HTTPS**: Enforced
- **Performance**: Production-like settings

#### Production Environment
- **Debug Mode**: Completely disabled
- **Analytics**: Full tracking enabled
- **Error Tracking**: Comprehensive monitoring
- **HTTPS**: Strictly enforced
- **Security Headers**: All enabled
- **Performance**: Fully optimized

---

## CI/CD Pipeline Integration

### Frontend CI Pipeline

The frontend CI pipeline (`ci-frontend.yml`) includes:

#### 1. Lint and Type Check
```yaml
- name: Run ESLint
  run: npm run lint

- name: Run Prettier check
  run: npm run format:check

- name: Run TypeScript type check
  run: npm run type-check
```

#### 2. Testing
```yaml
- name: Run tests
  run: npm test
  env:
    CI: true

- name: Upload test coverage
  uses: codecov/codecov-action@v4
```

#### 3. Build Process
```yaml
- name: Build application
  run: npm run build
  env:
    NODE_ENV: production
```

#### 4. Security Scanning
```yaml
- name: Run npm audit
  run: npm audit --audit-level moderate

- name: Run Snyk Security Check
  uses: snyk/actions/node@master
```

#### 5. Performance Testing
```yaml
- name: Lighthouse CI
  run: |
    npm install -g @lhci/cli@0.12.x
    lhci autorun
```

#### 6. Bundle Analysis
```yaml
- name: Analyze bundle
  run: |
    npm install -g @next/bundle-analyzer
    npx next-bundle-analyzer
```

### Deployment Pipeline

#### Production Deployment Process
1. **Image Building**: Multi-platform Docker images (AMD64, ARM64)
2. **ECR Push**: Automated push to AWS Elastic Container Registry
3. **ECS Deployment**: Blue-green deployment strategy
4. **Health Checks**: Automated health verification
5. **Rollback**: Automatic rollback on failure

#### Environment-Specific Deployments
- **Development**: Triggered on feature branch pushes
- **Staging**: Triggered on main branch pushes
- **Production**: Triggered on version tags

---

## Docker Configuration

### Multi-Stage Dockerfile

The frontend uses an optimized multi-stage Dockerfile:

#### Stage 1: Dependencies
```dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
```

#### Stage 2: Builder
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY . .
RUN npm ci
RUN npm run build
```

#### Stage 3: Runner
```dockerfile
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Copy built application
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000
CMD ["node", "server.js"]
```

### Docker Compose Integration

#### Development Configuration
```yaml
frontend:
  build:
    context: ./frontend/hanihome-au
    dockerfile: Dockerfile
  environment:
    NODE_ENV: development
    NEXT_PUBLIC_API_URL: http://localhost:8080/api/v1
    NEXT_PUBLIC_DEBUG_MODE: true
  ports:
    - "3000:3000"
```

#### Production Configuration
```yaml
frontend:
  image: ${ECR_REGISTRY}/hanihome-au-frontend:${IMAGE_TAG}
  environment:
    NODE_ENV: production
    NEXT_PUBLIC_API_URL: https://api.hanihome.com.au/api/v1
    NEXT_PUBLIC_FORCE_HTTPS: true
  deploy:
    resources:
      limits:
        memory: 1G
        cpus: '0.5'
```

---

## Production Optimization

### Next.js Configuration

#### next.config.js Enhancements
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  // Output configuration for Docker
  output: 'standalone',
  
  // Performance optimizations
  experimental: {
    optimizeCss: true,
    optimizePackageImports: ['@mui/material', 'lodash'],
  },
  
  // Image optimization
  images: {
    domains: ['hanihome-au-assets.s3.amazonaws.com'],
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
    ];
  },
  
  // Redirects for HTTPS enforcement
  async redirects() {
    if (process.env.NEXT_PUBLIC_FORCE_HTTPS === 'true') {
      return [
        {
          source: '/(.*)',
          has: [
            {
              type: 'header',
              key: 'x-forwarded-proto',
              value: 'http',
            },
          ],
          destination: 'https://hanihome.com.au/$1',
          permanent: true,
        },
      ];
    }
    return [];
  },
};

module.exports = nextConfig;
```

### Performance Optimizations

#### 1. Code Splitting
- **Route-based splitting**: Automatic Next.js page splitting
- **Component-based splitting**: Dynamic imports for heavy components
- **Library splitting**: Separate chunks for external libraries

#### 2. Image Optimization
- **WebP/AVIF formats**: Modern image formats for better compression
- **Responsive images**: Multiple sizes for different viewports
- **Lazy loading**: Images load only when needed

#### 3. Bundle Optimization
- **Tree shaking**: Remove unused code
- **Minification**: Compress JavaScript and CSS
- **Gzip compression**: Server-side compression

#### 4. Caching Strategy
- **Static assets**: Long-term caching with versioning
- **API responses**: Appropriate cache headers
- **Service worker**: Offline functionality

---

## Security Implementation

### Content Security Policy (CSP)

```javascript
const cspHeader = `
  default-src 'self';
  script-src 'self' 'unsafe-eval' 'unsafe-inline' *.googletagmanager.com;
  style-src 'self' 'unsafe-inline';
  img-src 'self' blob: data: *.googleapis.com *.gstatic.com;
  font-src 'self';
  connect-src 'self' *.google-analytics.com *.analytics.google.com;
  frame-src 'none';
`;
```

### Security Headers

```javascript
const securityHeaders = [
  {
    key: 'X-DNS-Prefetch-Control',
    value: 'on'
  },
  {
    key: 'Strict-Transport-Security',
    value: 'max-age=63072000; includeSubDomains; preload'
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
  }
];
```

### Authentication Security

#### NextAuth.js Configuration
```javascript
export default NextAuth({
  providers: [
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
    }),
    KakaoProvider({
      clientId: process.env.KAKAO_CLIENT_ID!,
      clientSecret: process.env.KAKAO_CLIENT_SECRET!,
    }),
  ],
  session: {
    strategy: 'jwt',
    maxAge: 30 * 24 * 60 * 60, // 30 days
  },
  jwt: {
    secret: process.env.NEXTAUTH_JWT_SECRET,
  },
  pages: {
    signIn: '/auth/signin',
    error: '/auth/error',
  },
  callbacks: {
    async jwt({ token, user, account }) {
      if (account && user) {
        token.accessToken = account.access_token;
        token.role = user.role;
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken;
      session.user.role = token.role;
      return session;
    },
  },
});
```

### Input Validation and Sanitization

#### Form Validation
```typescript
import { z } from 'zod';

const userProfileSchema = z.object({
  name: z.string().min(1).max(100),
  email: z.string().email(),
  phone: z.string().regex(/^[\d-+\s()]+$/),
  bio: z.string().max(500).optional(),
});

export type UserProfile = z.infer<typeof userProfileSchema>;
```

#### XSS Prevention
```typescript
import DOMPurify from 'dompurify';

const sanitizeHtml = (dirty: string): string => {
  return DOMPurify.sanitize(dirty);
};
```

---

## Monitoring and Analytics

### Google Analytics Integration

```typescript
// lib/gtag.ts
export const GA_TRACKING_ID = process.env.NEXT_PUBLIC_GA_ID;

export const pageview = (url: string) => {
  window.gtag('config', GA_TRACKING_ID, {
    page_path: url,
  });
};

export const event = ({ action, category, label, value }: {
  action: string;
  category: string;
  label?: string;
  value?: number;
}) => {
  window.gtag('event', action, {
    event_category: category,
    event_label: label,
    value: value,
  });
};
```

### Error Tracking with Sentry

```typescript
// sentry.client.config.ts
import * as Sentry from '@sentry/nextjs';

Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  tracesSampleRate: 1.0,
  environment: process.env.NODE_ENV,
  beforeSend(event) {
    // Filter out sensitive information
    if (event.exception) {
      const error = event.exception.values?.[0];
      if (error?.value?.includes('password')) {
        return null;
      }
    }
    return event;
  },
});
```

### Performance Monitoring

```typescript
// lib/performance.ts
export const reportWebVitals = (metric: any) => {
  if (process.env.NEXT_PUBLIC_ENABLE_ANALYTICS === 'true') {
    switch (metric.name) {
      case 'CLS':
      case 'FID':
      case 'FCP':
      case 'LCP':
      case 'TTFB':
        // Send to analytics
        window.gtag('event', metric.name, {
          value: Math.round(metric.value),
          event_category: 'Web Vitals',
        });
        break;
      default:
        break;
    }
  }
};
```

---

## Development Workflow Improvements

### Code Quality Tools

#### ESLint Configuration
```json
{
  "extends": [
    "next/core-web-vitals",
    "@typescript-eslint/recommended",
    "prettier"
  ],
  "rules": {
    "@typescript-eslint/no-unused-vars": "error",
    "@typescript-eslint/no-explicit-any": "warn",
    "react-hooks/exhaustive-deps": "error"
  }
}
```

#### Prettier Configuration
```json
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 80,
  "tabWidth": 2
}
```

### Testing Improvements

#### Jest Configuration
```javascript
const nextJest = require('next/jest');

const createJestConfig = nextJest({
  dir: './',
});

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  moduleNameMapping: {
    '^@/components/(.*)$': '<rootDir>/components/$1',
    '^@/pages/(.*)$': '<rootDir>/pages/$1',
  },
  testEnvironment: 'jest-environment-jsdom',
  collectCoverageFrom: [
    'components/**/*.{js,jsx,ts,tsx}',
    'pages/**/*.{js,jsx,ts,tsx}',
    '!**/*.d.ts',
    '!**/node_modules/**',
  ],
};

module.exports = createJestConfig(customJestConfig);
```

---

## Conclusion

This documentation covers the major improvements made to the HaniHome AU frontend during Tasks 1.5-1.7:

### Key Achievements
1. **Environment Management**: Comprehensive environment configuration system
2. **CI/CD Integration**: Automated testing, building, and deployment pipelines
3. **Production Optimization**: Performance and security optimizations
4. **Docker Integration**: Container-ready deployment configuration
5. **Monitoring Setup**: Analytics and error tracking implementation

### Next Steps
The frontend is now ready for:
- User authentication implementation (Task 2)
- Property listing features
- Real-time messaging system
- Advanced search functionality
- Payment integration

### Maintenance Notes
- Environment variables should be regularly audited
- Security headers should be tested after updates
- Performance metrics should be monitored continuously
- Dependencies should be kept up to date