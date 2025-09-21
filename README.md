# 프로젝트 목적
1. 자동화된 Claude Code의 개발 능력을 검증하는 것.
2. Claude Code를 활용하여 반복되는 (기능개발 -> 깃 관리 -> 문서화) 프로세스를 자동화하는 것

# 개발 환경
- Claude Code
- Taskmaster-ai MCP
- Github MCP
- Self Hosted Runner/Git-action

# Claude Code, Taskmaster-AI MCP, Github MCP를 조합한 개발 자동화 후기
1. 요구사항 -> 로드맵 제작
요구사항을 바탕으로 main task를 생성하고 sub task를 만들어서 제공한 전체 개발 로드맵의 품질은 꽤나 괜찮은 수준이라고 생각됨.

2. 코드 품질

    장점
    - 작은 수준의 코드 조각만 놓고 봤을 때는 나쁘지 않음
    - redis(캐싱), terraform(인프라배포), flyway(db ddl 변경반영) 등 툴들을 깊이와는 무관하게 일단 녹여내는 데에는 소질이 있는듯
    - 서비스에 필요하지만 놓치기 쉬운 요소들을 잘 고려함. // 걸러야할 욕설 키워드 등.
    - 내놓은 코드 자체가 구조적으로 리팩토링할 요소는 분명히 있음. but 현재 내 지식 이상의 내용에 대해서도 작성은 해놓는다.


     단점
    - 전체 프로젝트 수준에서 보았을 때는, 일부 클래스가 중복되는 문제 // context 제한이 가져오는 한계가 있음.
    - 롬복 어노테이션 사용이 비효율적임. @Data를 남발하거나 엔티티에도 @Setter를 열어둠
   
    -> 구체적인 전체 개발 컨벤션을 정하면 해결될듯
    DTO는 record 타입을 사용하도록 하고. 생성자는 static factory method를 둔다.

3. 문서화
- 문서 생성위치나 규칙을 프롬프트로 제공했음에도 claude code가 파일 위치를 제대로 놓지 못하는 문제가 가끔 발생함.

# 자동화 구조
![개발 자동화 구조](./프로젝트%20리뷰/개발%20자동화%20구조%20diagram.png?raw=true)

# HaniHome Australia 🏠

> Australian Real Estate Management Platform - Full-stack application with Next.js frontend and Spring Boot backend

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Frontend CI](https://github.com/WithFortuna/hanihome-au/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/WithFortuna/hanihome-au/actions/workflows/frontend-ci.yml)
[![Backend CI](https://github.com/WithFortuna/hanihome-au/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/WithFortuna/hanihome-au/actions/workflows/backend-ci.yml)

## 📖 Overview

HaniHome Australia is a comprehensive real estate management platform designed for the Australian market. It provides property search, management, and geographic-based filtering capabilities with a modern, responsive web interface.

### 🎯 Key Features

- **🔍 Property Search & Management**: Advanced property listing with filtering and search capabilities
- **🗺️ Geographic Search**: Location-based property search with Google Maps integration
- **🔐 Authentication System**: Secure JWT-based authentication with OAuth2 support (Google, Kakao)
- **👤 User Management**: Profile management and user preferences
- **📱 Responsive Design**: Mobile-first responsive web application
- **🏗️ Scalable Architecture**: Microservices-ready architecture with independent deployments

## 🏗️ Architecture

This is a **monorepo** containing multiple services:

```
hanihome-au/
├── 🎨 frontend/         # Next.js 15 + TypeScript + Tailwind CSS
├── ⚙️  backend/          # Spring Boot 3 + Java 21 + PostgreSQL
├── 🚀 infrastructure/   # Terraform AWS infrastructure
├── 📚 docs/            # Documentation
└── 🔧 scripts/         # Deployment and utility scripts
```

### 🛠️ Tech Stack

#### Frontend
- **Framework**: Next.js 15 with App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Authentication**: NextAuth.js v5
- **Maps**: Google Maps JavaScript API
- **State Management**: React Context + Hooks

#### Backend
- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21
- **Database**: PostgreSQL with PostGIS
- **Security**: Spring Security + JWT
- **Data**: JPA/Hibernate + QueryDSL
- **Cache**: Redis
- **Migration**: Flyway
- **Documentation**: OpenAPI 3 (Swagger)

#### Infrastructure
- **Cloud**: AWS (ECS, RDS, S3, CloudFront)
- **IaC**: Terraform
- **Containerization**: Docker
- **CI/CD**: GitHub Actions

## 🚀 Quick Start

### Prerequisites

- **Node.js** 18+ (for frontend)
- **Java** 21+ (for backend)
- **PostgreSQL** 14+ (for database)
- **Redis** 6+ (for caching)
- **Docker** (optional, for containerized development)

### 1. Clone the Repository

```bash
git clone https://github.com/WithFortuna/hanihome-au.git
cd hanihome-au
```

### 2. Environment Setup

Copy environment files and configure:

```bash
# Frontend
cp frontend/hanihome-au/.env.example frontend/hanihome-au/.env.local

# Backend
cp backend/hanihome-au-api/src/main/resources/application-local.yml.example \
   backend/hanihome-au-api/src/main/resources/application-local.yml
```

### 3. Database Setup

```bash
# Create PostgreSQL database
createdb hanihome_au

# Run the database initialization script
psql -d hanihome_au -f scripts/init-db.sql
```

### 4. Start Development Servers

#### Option A: Traditional Setup

```bash
# Terminal 1: Start Backend
cd backend/hanihome-au-api
./gradlew bootRun --args='--spring.profiles.active=local'

# Terminal 2: Start Frontend
cd frontend/hanihome-au
npm install
npm run dev
```

#### Option B: Docker Compose (Recommended)

```bash
# Start all services
docker-compose -f docker-compose.dev.yml up

# Or start specific services
docker-compose -f docker-compose.dev.yml up frontend backend
```

### 5. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html

## 📁 Project Structure

### Frontend (`frontend/hanihome-au/`)
```
src/
├── app/                 # Next.js App Router pages
├── components/          # Reusable React components
│   ├── auth/           # Authentication components
│   ├── maps/           # Google Maps components
│   ├── property/       # Property-related components
│   └── ui/             # UI components (buttons, etc.)
├── hooks/              # Custom React hooks
├── lib/                # Utility libraries and configurations
│   ├── api/           # API client configuration
│   ├── auth/          # Authentication utilities
│   └── maps/          # Maps utilities
└── middleware.ts       # Next.js middleware
```

### Backend (`backend/hanihome-au-api/`)
```
src/main/java/com/hanihome/
├── api/                # Common API components
│   ├── config/        # Configuration classes
│   ├── controller/    # REST controllers
│   ├── service/       # Business logic services
│   └── security/      # Security configurations
└── hanihome_au_api/   # Main application
    ├── domain/        # Domain entities and enums
    ├── dto/           # Data Transfer Objects
    ├── repository/    # Data access layer
    ├── service/       # Business services
    └── security/      # Security implementations
```

## 🔧 Development

### Frontend Development

```bash
cd frontend/hanihome-au

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run linting
npm run lint

# Run type checking
npm run type-check
```

### Backend Development

```bash
cd backend/hanihome-au-api

# Run application
./gradlew bootRun

# Build application
./gradlew build

# Run tests
./gradlew test

# Generate QueryDSL Q-classes
./gradlew compileQuerydsl
```

### Database Migrations

```bash
# Backend handles migrations automatically via Flyway
# Migration files are in: backend/hanihome-au-api/src/main/resources/db/migration/

# To create new migration:
# 1. Create V{number}__{description}.sql in migration folder
# 2. Restart the application
```

## 🚀 Deployment

### Staging Deployment

```bash
# Deploy to staging environment
docker-compose -f docker-compose.staging.yml up -d

# Or use deployment script
./scripts/deploy-infrastructure.sh staging
```

### Production Deployment

```bash
# Deploy infrastructure with Terraform
cd infrastructure/terraform
terraform init
terraform plan
terraform apply

# Deploy applications
docker-compose -f docker-compose.production.yml up -d
```

## 🧪 Testing

### Frontend Testing

```bash
cd frontend/hanihome-au
npm run test          # Run tests (when configured)
npm run test:watch    # Run tests in watch mode
npm run test:coverage # Run tests with coverage
```

### Backend Testing

```bash
cd backend/hanihome-au-api
./gradlew test                    # Run all tests
./gradlew test --tests "*Unit*"  # Run unit tests only
./gradlew test --tests "*Integration*"  # Run integration tests only
```

## 📊 Monitoring & Health Checks

### Health Endpoints

- **Backend Health**: `GET /actuator/health`
- **Frontend Health**: `GET /api/health`

### Monitoring

- **Backend Metrics**: Available at `/actuator/metrics`
- **Frontend Monitoring**: Built-in Next.js analytics
- **Database Monitoring**: PostgreSQL performance insights

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

### Development Guidelines

- Follow existing code style and patterns
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure CI/CD pipelines pass

## 📚 API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Postman Collection**: Available in `/docs/api/` directory

## 🔐 Security

- JWT-based authentication
- OAuth2 integration (Google, Kakao)
- CORS configuration
- Input validation and sanitization
- SQL injection protection via JPA/QueryDSL
- Security headers configuration

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

For support and questions:

- **Issues**: [GitHub Issues](https://github.com/WithFortuna/hanihome-au/issues)
- **Discussions**: [GitHub Discussions](https://github.com/WithFortuna/hanihome-au/discussions)
- **Documentation**: [Wiki](https://github.com/WithFortuna/hanihome-au/wiki)

## 🗺️ Roadmap

- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Property valuation AI
- [ ] Integration with Australian property APIs
- [ ] Advanced search filters
- [ ] Property comparison features

---

**Made with ❤️ for the Australian real estate market**
