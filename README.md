# Ground & Grow Psychology - Psychology Services Platform

A full-stack web application for managing psychology services in Australia, providing online, in-person, and phone counseling.

## Project Overview

**Ground & Grow Psychology** is a dual-portal web application designed for:
- **Patients**: Book appointments, manage sessions, and access resources
- **Psychologists**: Manage schedules, view appointments, and handle client interactions

## Tech Stack

### Frontend
- TypeScript
- React 18
- React Router (client-side routing)
- Redux Toolkit (state management)
- Vite (build tool)
- Tailwind CSS (styling)
- Axios (HTTP client)

### Backend
- Java 17+
- Spring Boot 3.x
- Spring Security (authentication/authorization)
- Spring Data MongoDB
- Maven (build tool)
- JWT (token-based auth)
- Lombok

### Database
- MongoDB

### DevOps
- Docker & Docker Compose
- Git version control

## Project Structure

```
groundandgrow/
├── backend/                      # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/groundandgrow/
│   │   │   │   ├── config/      # Configuration classes
│   │   │   │   ├── controller/  # REST controllers
│   │   │   │   ├── model/       # MongoDB entities
│   │   │   │   ├── repository/  # Data access layer
│   │   │   │   ├── service/     # Business logic
│   │   │   │   ├── dto/         # Data transfer objects
│   │   │   │   ├── security/    # Security config
│   │   │   │   └── exception/   # Custom exceptions
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/                # Unit and integration tests
│   ├── pom.xml
│   └── .env.example
│
├── frontend/                     # React application
│   ├── src/
│   │   ├── components/          # Reusable components
│   │   ├── pages/               # Page components
│   │   ├── services/            # API services
│   │   ├── store/               # Redux store
│   │   ├── hooks/               # Custom hooks
│   │   ├── utils/               # Utility functions
│   │   ├── types/               # TypeScript types
│   │   └── assets/              # Images, fonts, etc.
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── .env.example
│
├── docker/                       # Docker configurations
└── .claude/                      # Claude agent context
    └── context.md
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- MongoDB (local or MongoDB Atlas)
- Git

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

3. Update `.env` with your configuration (MongoDB URI, JWT secret, etc.)

4. Install dependencies and build:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

6. Access API documentation:
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - OpenAPI docs: `http://localhost:8080/api/api-docs`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

3. Install dependencies:
   ```bash
   npm install
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173`

### Running with Docker (Coming Soon)

```bash
docker-compose up
```

## Development Workflow

### Backend Development

- **Run tests**: `mvn test`
- **Build**: `mvn clean package`
- **Run with dev profile**: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

### Frontend Development

- **Start dev server**: `npm run dev`
- **Build for production**: `npm run build`
- **Preview production build**: `npm run preview`
- **Run tests**: `npm test`
- **Lint code**: `npm run lint`

## API Endpoints (Planned)

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Appointments
- `GET /api/appointments` - List appointments
- `POST /api/appointments` - Create appointment
- `GET /api/appointments/{id}` - Get appointment details
- `PUT /api/appointments/{id}` - Update appointment
- `DELETE /api/appointments/{id}` - Cancel appointment

### Psychologists
- `GET /api/psychologists` - List psychologists
- `GET /api/psychologists/{id}` - Get psychologist details
- `GET /api/psychologists/{id}/availability` - Get availability

### Clients (Patient Portal)
- `GET /api/clients/profile` - Get client profile
- `PUT /api/clients/profile` - Update client profile
- `GET /api/clients/appointments` - Get client appointments

## Features (Planned)

### Patient Portal
- [ ] User registration and authentication
- [ ] Browse available psychologists
- [ ] Book appointments (online/in-person/phone)
- [ ] View appointment history
- [ ] Manage profile
- [ ] Payment processing
- [ ] Secure messaging

### Psychologist Portal
- [ ] Professional authentication
- [ ] Dashboard with upcoming appointments
- [ ] Calendar management
- [ ] Set availability and time off
- [ ] View client appointments
- [ ] Session notes
- [ ] Analytics and reporting

## Environment Variables

### Backend (.env)
```
MONGODB_URI=mongodb://localhost:27017/groundandgrow
JWT_SECRET=your-secret-key
ADMIN_USERNAME=admin
ADMIN_PASSWORD=changeme
CORS_ORIGINS=http://localhost:5173
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Ground & Grow Psychology
```

## Development Roadmap

**For in-house development tasks (no 3rd party subscriptions required), see:**
- **IN_HOUSE_DEVELOPMENT_ROADMAP.md** - Comprehensive task list and timeline

**High Priority Features:**
- **ZOOM_CALENDAR_INTEGRATION_ROADMAP.md** - 🔥 HIGH PRIORITY: Zoom meetings & Google Calendar integration for appointment flow

**Quick Start for Developers:**
1. Set up local MongoDB
2. Configure mock services (email, SMS, payment)
3. Run tests with local environment
4. See roadmap for detailed phase-by-phase guide

## Contributing

This project is built with Claude Code AI agents. Follow established patterns and conventions when adding new features.

## Security Notes

- Never commit `.env` files
- Change default passwords in production
- Use strong JWT secrets (minimum 256 bits)
- Enable HTTPS in production
- Follow OWASP security guidelines
- Comply with Australian healthcare data regulations

## License

Private and confidential.

## Support

For questions or issues, contact the development team.
