# Healthcare Management System

A full-stack web application built as a Final Year Project that digitalizes and simplifies hospital operations. It connects **Admins**, **Doctors**, and **Patients** in one secure, role-based platform.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Frontend | Thymeleaf, HTML5, CSS3 |
| Database | PostgreSQL |
| Security | Spring Security (BCrypt, CSRF, Session) |
| PDF Generation | iText PDF |
| Charts | Chart.js |
| Build Tool | Maven |

---

## Features

### Authentication & Security
- Role-based login for Admin, Doctor, and Patient
- BCrypt password encryption
- CSRF protection on all forms
- No-cache headers on protected routes — prevents back-button access after logout
- Session invalidation on logout

---

### Admin Dashboard
- Overview cards — Total Doctors, Total Patients, Today's Appointments
- **Appointments** — Create, search, filter (by date/doctor/patient), update status (Pending → Approved → Completed), delete
- **Doctors** — Add, edit, delete doctors with full profile details
- **Patients** — Add, view, delete patients
- Completed appointments auto-move to bottom of today's table

---

### Doctor Dashboard
- Overview cards — Today's appointments count, Total appointments
- **Profile card** with specialization, qualification, and experience details
- **Appointment chart** — Bar graph of appointments grouped by date (Chart.js)
- **Calendar** — Mark and manage personal off days
- **Appointments** — View own appointments; newly created ones go to Pending (admin must approve, doctor cannot approve own appointments)
- **Records** — Add and view medical records for own patients only (other doctors' patients are hidden); includes BP, sugar level, weight, temperature, heart rate, diagnosis, tests, and notes
- **Medications** — Prescribe medicines linked to patient records; deactivate prescriptions
- **Download** — Generate and download a patient's full medical report as PDF

---

### Patient Dashboard
- Overview cards — Total appointments, Medical records count, Active medications
- **Appointments** — View upcoming appointments and full history; book new appointments (submitted for admin approval)
- **Medical Records** — View all personal records added by doctors with complete vitals and diagnosis
- **Medications** — View active and historical prescriptions with dosage, frequency, and instructions
- **Profile** — Edit personal details (gender, blood group, address, allergies, emergency contact)
- **Download** — Download complete personal medical report as a formatted PDF

---

## Project Structure

```
healthcare-management/
├── src/main/java/com/healthcare/
│   ├── config/
│   │   ├── SecurityConfig.java        # Spring Security config
│   │   ├── DataInitializer.java       # Seeds default users on startup
│   │   └── NoCacheFilter.java         # Prevents back-button cache exploit
│   ├── controller/
│   │   ├── HomeController.java
│   │   ├── AuthController.java
│   │   ├── AdminController.java
│   │   ├── DoctorController.java
│   │   └── PatientController.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Doctor.java
│   │   ├── Patient.java
│   │   ├── Appointment.java
│   │   ├── MedicalRecord.java
│   │   ├── Medication.java
│   │   ├── Role.java (enum)
│   │   └── AppointmentStatus.java (enum)
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic layer
│   │   ├── UserService.java
│   │   ├── DoctorService.java
│   │   ├── PatientService.java
│   │   ├── AppointmentService.java
│   │   ├── MedicalRecordService.java
│   │   └── PdfService.java            # iText PDF report generation
│   └── HealthcareApplication.java
│
├── src/main/resources/
│   ├── templates/
│   │   ├── index.html                 # Public home page
│   │   ├── auth/                      # Login & Register
│   │   ├── admin/                     # Admin pages
│   │   ├── doctor/                    # Doctor pages
│   │   └── patient/                   # Patient pages
│   ├── static/css/style.css           # Custom teal-themed CSS
│   └── application.properties
│
└── pom.xml
```

---

## Setup & Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/your-username/healthcare-management.git
cd healthcare-management
```

**2. Create the database**
```sql
CREATE DATABASE healthcare_db;
```

**3. Configure database credentials**

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/healthcare_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Open in browser**
```
http://localhost:8080
```

---

## Default Login Credentials

These accounts are automatically created on first startup:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@healthcare.com | admin123 |
| Doctor | doctor@healthcare.com | doctor123 |
| Patient | patient@healthcare.com | patient123 |

> Change these credentials before deploying to production.

---

## 📸 Screenshots

| Page | Description |
|------|-------------|
| Home Page | Public landing page with services overview |
| Login | Role-based authentication |
| Admin Dashboard | Stats, today's appointments with status control |
| Doctor Dashboard | Profile card, appointment chart, calendar |
| Patient Dashboard | Appointments, records, medications summary |

---

## Database Schema (Key Tables)

```
users           → id, name, email, password, phone, role, active
doctors         → id, user_id, department, specialization, qualification, experience
patients        → id, user_id, gender, blood_group, address, allergies
appointments    → id, patient_id, doctor_id, date, time, symptoms, status
medical_records → id, patient_id, doctor_id, bp, sugar, weight, diagnosis, tests
medications     → id, patient_id, doctor_id, record_id, medicine, dosage, frequency, active
doctor_off_days → doctor_id, off_date
```

---

## Security Notes

- All passwords are hashed with **BCrypt**
- **CSRF tokens** are included in every POST form via Thymeleaf
- Protected routes require an active authenticated session
- **Cache-Control headers** (`no-store, no-cache, must-revalidate`) are set on all dashboard routes to prevent back-button access after logout
- Doctors can only access **their own patients'** records
- Doctors **cannot approve** their own appointments — only Admin can

---

## License

This project is developed as a **Final Year Academic Project**.  
Feel free to use it for learning and reference purposes.
