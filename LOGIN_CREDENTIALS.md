# Login Credentials for Testing

## Psychologist Accounts

All psychologists have the same password: **`password123`**

### Available Accounts:

1. **Dr. Sarah Thompson**
   - Email: `sarah.thompson@groundandgrow.com.au`
   - Password: `password123`
   - Specialization: Anxiety, Depression, Trauma

2. **Michael Chen**
   - Email: `michael.chen@groundandgrow.com.au`
   - Password: `password123`
   - Specialization: Relationship Issues, Stress Management, Work-Life Balance

3. **Emma Wilson**
   - Email: `emma.wilson@groundandgrow.com.au`
   - Password: `password123`
   - Specialization: Child & Adolescent Psychology, Family Therapy

## Application URLs

### Frontend (Currently running on PORT 5174 - port 5173 was in use)
- **Patient Portal:** http://localhost:5174/
- **Psychologist Login:** http://localhost:5174/psychologist/login
- **Dashboard:** http://localhost:5174/psychologist/dashboard

### Backend API
- **Base URL:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui.html

## Quick Test

```bash
# Login and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sarah.thompson@groundandgrow.com.au","password":"password123"}'

# Use the token to access dashboard (replace YOUR_TOKEN)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/psychologist/dashboard
```

## NOTE

There is **NO** `admin@groundandgrow.com.au` account. The sample data only includes the 3 psychologists listed above.
