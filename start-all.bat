@echo off
echo ==========================================
echo Starting all microservices...
echo Ensure MongoDB is running on port 27017.
echo ==========================================

echo Starting Auth Service on port 8083...
start "Auth Service" cmd /k "cd /d c:\Users\Vasu\Documents\auth-service && mvnw spring-boot:run"

timeout /t 3 /nobreak > nul

echo Starting NGO Service on port 8080...
start "NGO Service" cmd /k "cd /d c:\Users\Vasu\Documents\ngo-service && mvnw spring-boot:run"

timeout /t 3 /nobreak > nul

echo Starting Donation Service on port 8082...
start "Donation Service" cmd /k "cd /d c:\Users\Vasu\Documents\donation-service && mvnw spring-boot:run"

timeout /t 3 /nobreak > nul

echo Starting API Gateway on port 8081...
start "API Gateway" cmd /k "cd /d c:\Users\Vasu\Documents\api-gateway && mvnw spring-boot:run"

echo All services are starting up!
pause
