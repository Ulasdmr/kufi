# Kufi Component Interfaces and Data Model

## 1. Representative API Endpoint Interface
- **Request:** `POST /api/v1/appointments`
- **Body:** `{"salonId": 12, "serviceId": 7, "startTime": "14:00"}`
- **Success Response:** `HTTP 201 Created`
- **Response Body:** `{"appointmentId": 1042, "status": "CONFIRMED"}`
- **Error Response:** `HTTP 409 Conflict` (If slot is already booked)

## 2. Preliminary Data Dictionary (PostgreSQL)
- **users:** `id` (PK), `full_name`, `email` (UNIQUE), `password_hash`, `role`.
- **salons:** `id` (PK), `owner_id` (FK -> users), `name`, `district` (INDEXED).
- **services:** `id` (PK), `salon_id` (FK -> salons), `name`, `duration_minutes`, `price`.
- **appointments:** `id` (PK), `customer_id` (FK), `salon_id` (FK), `appointment_date`, `start_time`, `status`. (Unique Partial Index on salon_id, date, time where status is CONFIRMED).
