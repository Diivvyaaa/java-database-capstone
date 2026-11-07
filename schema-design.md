## MySQL Database Design

## Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password: VARCHAR(255), Not Null   <!-- stores a BCrypt hash, never plain text -->

## Table: clinic_locations
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- address: VARCHAR(255), Not Null
- phone: VARCHAR(15)

## Table: doctors
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- specialization: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(15)
- clinic_location_id: INT, Foreign Key → clinic_locations(id)
- is_active: BOOLEAN, Not Null, Default TRUE

## Table: patients
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(15)
- address: VARCHAR(255)
- created_at: DATETIME, Not Null, Default CURRENT_TIMESTAMP
- is_active: BOOLEAN, Not Null, Default TRUE

## Table: doctor_availability
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Not Null, Foreign Key → doctors(id) ON DELETE CASCADE
- start_time: DATETIME, Not Null
- end_time: DATETIME, Not Null
- is_available: BOOLEAN, Not Null, Default TRUE   <!-- FALSE = doctor marked this period as unavailable -->

## Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Not Null, Foreign Key → doctors(id) ON DELETE RESTRICT
- patient_id: INT, Not Null, Foreign Key → patients(id) ON DELETE RESTRICT
- appointment_time: DATETIME, Not Null
- status: INT, Not Null, Default 0 (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- created_at: DATETIME, Not Null, Default CURRENT_TIMESTAMP
- Constraint: UNIQUE (doctor_id, appointment_time)

## Table: payments
- id: INT, Primary Key, Auto Increment
- appointment_id: INT, Not Null, Unique, Foreign Key → appointments(id) ON DELETE RESTRICT
- amount: DECIMAL(10,2), Not Null
- payment_method: VARCHAR(30)   <!-- e.g. CARD, UPI, CASH -->
- payment_status: INT, Not Null, Default 0 (0 = Pending, 1 = Paid, 2 = Refunded)
- paid_at: DATETIME


## MongoDB Collection Design
## Collection (MongoDB): prescriptions
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
