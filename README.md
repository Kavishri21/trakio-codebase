# Trakio Codebase

Monorepo containing frontend and backend for the Trakio project.

## Structure

| Folder | Description | Tech Stack |
|--------|-------------|------------|
| `trakio-frontend/` | React frontend | React, Vite, Redux |
| `trakio-backend/` | Spring Boot API | Java, Spring Boot, PostgreSQL |

## Development

### Frontend
```bash
cd trakio-frontend
npm install
npm run dev

cd trakio-backend
./mvnw spring-boot:run
