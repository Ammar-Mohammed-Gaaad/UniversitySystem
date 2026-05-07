-- Fix role values in database for existing records
UPDATE students SET role = 'STUDENT' WHERE role = 'USER' OR role = 'LECTURER';
UPDATE lecturers SET role = 'LECTURER' WHERE role = 'USER';
