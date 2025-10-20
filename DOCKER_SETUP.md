# Docker Setup Guide

Run the Ground & Grow Psychology booking system using Docker on any platform (Windows, macOS, Linux).

**Perfect for M1/M2 Mac Air** - all images are multi-architecture compatible!

## Prerequisites

### 1. Install Docker Desktop

**For M1/M2 Mac:**
- Download: https://www.docker.com/products/docker-desktop/
- Install Docker Desktop for Mac (Apple Silicon)
- Start Docker Desktop

**For Windows:**
- Download: https://www.docker.com/products/docker-desktop/
- Install Docker Desktop for Windows
- Ensure WSL2 backend is enabled

**For Linux:**
- Install Docker Engine: https://docs.docker.com/engine/install/
- Install Docker Compose: https://docs.docker.com/compose/install/

### 2. Verify Installation

```bash
docker --version
docker-compose --version
```

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/SektorR/clinic-booking-system.git
cd clinic-booking-system
```

### 2. Set Up Environment Variables

```bash
cd docker
cp .env.example .env
```

**Edit `.env` if needed** (optional - defaults work for local development)

### 3. Start All Services

```bash
# From the docker directory
docker-compose up -d
```

**Or from the project root:**
```bash
cd clinic-booking-system
docker-compose -f docker/docker-compose.yml up -d
```

This will:
- ✅ Pull MongoDB image
- ✅ Build backend Spring Boot application
- ✅ Build frontend React application
- ✅ Start all services in the background

### 4. Access the Application

**Wait 30-60 seconds for services to initialize**, then:

- **Patient Portal:** http://localhost:5173
- **Psychologist Login:** http://localhost:5173/psychologist/login
- **Backend API:** http://localhost:8080/api
- **API Docs:** http://localhost:8080/api/swagger-ui.html

**Default Login:**
- Username: `admin`
- Password: `admin123`

## Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mongodb
```

### Stop Services
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

### Rebuild After Code Changes
```bash
# Rebuild all services
docker-compose up -d --build

# Rebuild specific service
docker-compose up -d --build backend
```

### Restart a Service
```bash
docker-compose restart backend
docker-compose restart frontend
```

## Troubleshooting

### Services Not Starting

**Check logs:**
```bash
docker-compose logs backend
```

**Common issues:**
- Port 8080 or 5173 already in use
- MongoDB connection issues
- Build errors

### Port Already in Use

**Change ports in `docker-compose.yml`:**
```yaml
ports:
  - "8081:8080"  # Use 8081 instead of 8080
```

### Backend Can't Connect to MongoDB

Ensure MongoDB is healthy:
```bash
docker-compose ps
docker-compose logs mongodb
```

### Build Fails on M1/M2 Mac

Docker should automatically use ARM architecture. If issues persist:
```bash
docker-compose build --no-cache
```

### Clear Everything and Start Fresh

```bash
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

## Development Workflow

### Make Code Changes

**Backend (Java):**
1. Edit files in `backend/src/`
2. Rebuild: `docker-compose up -d --build backend`

**Frontend (React/TypeScript):**
1. Edit files in `frontend/src/`
2. Rebuild: `docker-compose up -d --build frontend`

### Access Database

```bash
# Connect to MongoDB shell
docker exec -it groundandgrow-mongodb mongosh

# Use the database
use groundandgrow

# View collections
show collections

# Query data
db.psychologists.find()
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│  Docker Network: groundandgrow-network          │
│                                                  │
│  ┌──────────────┐  ┌──────────────┐            │
│  │   Frontend   │  │   Backend    │            │
│  │  (Nginx)     │→ │ (Spring Boot)│            │
│  │  Port: 80    │  │  Port: 8080  │            │
│  └──────────────┘  └──────┬───────┘            │
│         ↓                  ↓                     │
│     localhost:5173    localhost:8080            │
│                            ↓                     │
│                    ┌──────────────┐             │
│                    │   MongoDB    │             │
│                    │  Port: 27017 │             │
│                    └──────────────┘             │
│                            ↓                     │
│                   Volume: mongodb_data          │
└─────────────────────────────────────────────────┘
```

## Environment Variables

See `docker/.env.example` for all available options:

- `JWT_SECRET` - JWT signing key
- `ADMIN_USERNAME` / `ADMIN_PASSWORD` - Admin credentials
- `EMAIL_ENABLED` - Enable/disable email notifications
- `SMS_ENABLED` - Enable/disable SMS notifications
- `STRIPE_API_KEY` - Stripe payment integration

## Production Deployment

For production, update:

1. **Environment variables** in `.env`
2. **CORS_ORIGINS** to your production domain
3. **JWT_SECRET** to a secure random string
4. **Database** to MongoDB Atlas or managed service
5. **Reverse proxy** (nginx, Traefik) for SSL/TLS

## Platform-Specific Notes

### M1/M2 Mac (Apple Silicon)
✅ Fully supported - uses ARM64 images automatically

### Intel Mac / Windows / Linux
✅ Fully supported - uses AMD64 images

### Rosetta (M1 Mac)
Not needed - native ARM64 support!

## Need Help?

- View logs: `docker-compose logs -f`
- Check status: `docker-compose ps`
- Restart: `docker-compose restart`
- Clean slate: `docker-compose down -v && docker-compose up -d --build`

---

**Happy coding!** 🚀
