# User Story Template
1.As an admin, I want to log out of the portal, so that system access is protected.

Acceptance Criteria:

A logout option is visible on the Admin Dashboard
Clicking logout ends the session/invalidates the token
After logout, admin pages redirect to the login page

Priority: High
Story Points: 2
Notes:

Back button must not reveal protected pages after logout

2.As an admin, I want to delete a doctor’s profile from the portal, so that inactive or invalid doctors are removed.

Acceptance Criteria:

Admin can select a doctor and confirm deletion
Doctor record is removed from the MySQL database
Deleted doctor no longer appears in the patient doctor list

Priority: Medium
Story Points: 3
Notes:

Decide how to handle existing appointments of the deleted doctor
Show a confirmation dialog before deleting'

3.As an admin, I want to add doctors to the portal, so that patients can find and book them.

Acceptance Criteria:

Admin can submit a form with doctor name, specialization, email, phone and password
Doctor is saved in the MySQL database
Duplicate email is rejected with a clear message

Priority: High
Story Points: 5
Notes:

Validate all required fields
Only admins can access this action


4.As an admin, I want to delete a doctor’s profile from the portal, so that inactive or invalid doctors are removed.

Acceptance Criteria:

Admin can select a doctor and confirm deletion
Doctor record is removed from the MySQL database
Deleted doctor no longer appears in the patient doctor list

Priority: Medium
Story Points: 3
Notes:

Decide how to handle existing appointments of the deleted doctor
Show a confirmation dialog before deleting

5.As an admin, I want to run a stored procedure in the MySQL CLI to get the number of appointments per month, so that I can track usage statistics.

Acceptance Criteria:

A stored procedure is created in MySQL
Running it in the MySQL CLI returns appointment count grouped by month
Months with zero appointments are handled correctly

Priority: Medium
Story Points: 5
Notes:

Include the .sql script in the repo
Consider adding a year parameter

#patient

1.As a patient, I want to sign up using my email and password, so that I can book appointments.

Acceptance Criteria:

Sign-up form accepts name, email and password
Email format and password strength are validated
Duplicate email shows an error
Successful sign-up creates a Patient record in MySQL

Priority: High
Story Points: 5
Notes:

Hash passwords before saving

































