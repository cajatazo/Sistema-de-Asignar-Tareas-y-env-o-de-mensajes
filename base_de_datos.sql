-- Crear base de datos
CREATE DATABASE IF NOT EXISTS gestion_tareas;
USE gestion_tareas;

-- Tabla de usuarios
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

-- Tabla de cursos
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    code VARCHAR(10) UNIQUE NOT NULL,
    teacher_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Tabla intermedia para estudiantes en cursos
CREATE TABLE course_students (
    course_id BIGINT,
    student_id BIGINT,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Tabla de tareas
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    due_date TIMESTAMP NOT NULL,
    points INT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    course_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Tabla de entregas
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment VARCHAR(500),
    file_path VARCHAR(255),
    file_name VARCHAR(255),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grade INT,
    feedback VARCHAR(1000),
    status ENUM('SUBMITTED', 'UNDER_REVIEW', 'GRADED', 'RETURNED') DEFAULT 'SUBMITTED',
    task_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Tabla de mensajes
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Tabla de notificaciones
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    type ENUM('TASK_ASSIGNED', 'TASK_SUBMITTED', 'TASK_GRADED', 'MESSAGE_RECEIVED', 'SYSTEM_ANNOUNCEMENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 🔥 NUEVAS TABLAS AGREGADAS BASADAS EN EL CÓDIGO COMPLETO

-- Tabla para auditoría de acciones del sistema
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    user_id BIGINT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabla para configuración del sistema
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 🔥 NUEVOS ÍNDICES PARA MEJORAR RENDIMIENTO
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_active ON courses(active);
CREATE INDEX idx_tasks_course_id ON tasks(course_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_submissions_task_id ON submissions(task_id);
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 🔥 NUEVOS DATOS INICIALES MEJORADOS

-- Insertar usuarios iniciales con contraseñas hasheadas (password: 123456)
INSERT INTO users (first_name, last_name, email, password, role) VALUES
('Admin', 'Sistema', 'admin@sistema.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'ADMIN'),
('María', 'García', 'maria.garcia@escuela.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'TEACHER'),
('Carlos', 'López', 'carlos.lopez@escuela.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'TEACHER'),
('Ana', 'Martínez', 'ana.martinez@estudiante.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'STUDENT'),
('Pedro', 'Rodríguez', 'pedro.rodriguez@estudiante.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'STUDENT'),
('Laura', 'Hernández', 'laura.hernandez@estudiante.com', '$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJK', 'STUDENT');

-- Insertar cursos iniciales
INSERT INTO courses (name, description, code, teacher_id) VALUES
('Matemáticas Básicas', 'Curso introductorio de matemáticas para principiantes', 'MATH101', 2),
('Programación Java', 'Fundamentos de programación orientada a objetos con Java', 'JAVA101', 2),
('Base de Datos SQL', 'Administración y consultas en bases de datos relacionales', 'SQL201', 3),
('Desarrollo Web', 'Creación de aplicaciones web modernas con HTML, CSS y JavaScript', 'WEB101', 3);

-- Inscribir estudiantes en cursos
INSERT INTO course_students (course_id, student_id) VALUES
(1, 4), (1, 5), -- Matemáticas: Ana y Pedro
(2, 4), (2, 6), -- Java: Ana y Laura
(3, 5), (3, 6), -- SQL: Pedro y Laura
(4, 4), (4, 5), (4, 6); -- Web: Todos los estudiantes

-- Insertar tareas de ejemplo
INSERT INTO tasks (title, description, due_date, points, course_id) VALUES
('Ejercicios de Álgebra', 'Resolver los ejercicios 1-20 del capítulo 2', DATE_ADD(NOW(), INTERVAL 7 DAY), 100, 1),
('Proyecto Calculadora', 'Crear una calculadora básica en Java', DATE_ADD(NOW(), INTERVAL 14 DAY), 150, 2),
('Consultas SQL', 'Realizar 10 consultas sobre la base de datos de ejemplo', DATE_ADD(NOW(), INTERVAL 10 DAY), 120, 3),
('Página Web Personal', 'Diseñar y desarrollar una página web personal', DATE_ADD(NOW(), INTERVAL 21 DAY), 200, 4),
('Examen Parcial Matemáticas', 'Examen covering chapters 1-4', DATE_ADD(NOW(), INTERVAL 5 DAY), 300, 1);

-- Insertar algunas entregas de ejemplo
INSERT INTO submissions (comment, file_path, file_name, task_id, student_id, grade, feedback, status) VALUES
('He completado los ejercicios 1-15', '/uploads/submissions/algebra_ana.pdf', 'algebra_ana.pdf', 1, 4, 85, 'Buen trabajo, pero faltan los ejercicios 16-20', 'GRADED'),
('Calculadora funcional con operaciones básicas', '/uploads/submissions/calculadora_pedro.zip', 'calculadora_pedro.zip', 2, 5, NULL, NULL, 'SUBMITTED'),
('Consultas SQL realizadas', NULL, NULL, 3, 6, 95, 'Excelente trabajo en todas las consultas', 'GRADED');

-- Insertar mensajes de ejemplo
INSERT INTO messages (subject, content, sender_id, receiver_id) VALUES
('Duda sobre ejercicio 15', 'Profesora, tengo una duda sobre cómo resolver el ejercicio 15 de álgebra', 4, 2),
('Consulta sobre el proyecto', 'Hola Carlos, quería consultarte sobre los requisitos del proyecto de base de datos', 6, 3),
('Bienvenida al curso', 'Bienvenido al curso de Matemáticas Básicas. Estoy aquí para ayudarte en lo que necesites', 2, 4);

-- Insertar notificaciones de ejemplo
INSERT INTO notifications (title, message, type, user_id) VALUES
('Nueva Tarea Asignada', 'Se ha asignado una nueva tarea: Ejercicios de Álgebra', 'TASK_ASSIGNED', 4),
('Tarea Calificada', 'Tu tarea "Ejercicios de Álgebra" ha sido calificada: 85 puntos', 'TASK_GRADED', 4),
('Nuevo Mensaje', 'Tienes un nuevo mensaje de Profesora María', 'MESSAGE_RECEIVED', 4),
('Entrega Recibida', 'El estudiante Pedro Rodríguez ha entregado la tarea: Proyecto Calculadora', 'TASK_SUBMITTED', 2);

-- 🔥 NUEVOS DATOS DE CONFIGURACIÓN DEL SISTEMA
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('MAX_FILE_SIZE', '10485760', 'Tamaño máximo de archivo en bytes (10MB)'),
('ALLOWED_FILE_TYPES', 'pdf,doc,docx,zip,rar,jpg,png', 'Tipos de archivo permitidos para subida'),
('SESSION_TIMEOUT', '1800', 'Tiempo de expiración de sesión en segundos'),
('PAGINATION_SIZE', '10', 'Número de elementos por página en listados'),
('SYSTEM_EMAIL', 'sistema@escuela.com', 'Email del sistema para notificaciones'),
('ACCOUNT_CREATION', 'true', 'Permitir creación de nuevas cuentas');

-- 🔥 DATOS DE AUDITORÍA INICIALES
INSERT INTO audit_log (action, description, user_id, ip_address) VALUES
('SYSTEM_START', 'Sistema iniciado correctamente', 1, '127.0.0.1'),
('USER_LOGIN', 'Usuario admin ha iniciado sesión', 1, '127.0.0.1'),
('COURSE_CREATED', 'Curso Matemáticas Básicas creado', 2, '127.0.0.1');

-- 🔥 VISTAS ÚTILES PARA REPORTES

-- Vista para tareas pendientes por estudiante
CREATE VIEW student_pending_tasks AS
SELECT 
    u.id as student_id,
    u.first_name,
    u.last_name,
    t.id as task_id,
    t.title,
    t.due_date,
    c.name as course_name
FROM users u
JOIN course_students cs ON u.id = cs.student_id
JOIN courses c ON cs.course_id = c.id
JOIN tasks t ON c.id = t.course_id
LEFT JOIN submissions s ON t.id = s.task_id AND u.id = s.student_id
WHERE u.role = 'STUDENT' 
AND u.active = TRUE
AND t.due_date > NOW()
AND s.id IS NULL;

-- Vista para estadísticas de profesores
CREATE VIEW teacher_statistics AS
SELECT 
    u.id as teacher_id,
    u.first_name,
    u.last_name,
    COUNT(DISTINCT c.id) as total_courses,
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT cs.student_id) as total_students,
    COUNT(DISTINCT s.id) as total_submissions
FROM users u
LEFT JOIN courses c ON u.id = c.teacher_id
LEFT JOIN tasks t ON c.id = t.course_id
LEFT JOIN course_students cs ON c.id = cs.course_id
LEFT JOIN submissions s ON t.id = s.task_id
WHERE u.role = 'TEACHER'
AND u.active = TRUE
GROUP BY u.id, u.first_name, u.last_name;

-- Vista para notificaciones no leídas
CREATE VIEW unread_notifications AS
SELECT 
    n.id,
    n.title,
    n.message,
    n.type,
    n.created_at,
    u.first_name,
    u.last_name,
    u.email
FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE n.is_read = FALSE
ORDER BY n.created_at DESC;

-- 🔥 PROCEDIMIENTOS ALMACENADOS ÚTILES

-- Procedimiento para marcar notificaciones como leídas
DELIMITER //
CREATE PROCEDURE MarkNotificationsAsRead(IN user_id_param BIGINT)
BEGIN
    UPDATE notifications 
    SET is_read = TRUE 
    WHERE user_id = user_id_param 
    AND is_read = FALSE;
END //
DELIMITER ;

-- Procedimiento para obtener estadísticas del dashboard
DELIMITER //
CREATE PROCEDURE GetDashboardStats(IN user_id_param BIGINT)
BEGIN
    DECLARE user_role VARCHAR(20);
    
    SELECT role INTO user_role FROM users WHERE id = user_id_param;
    
    IF user_role = 'ADMIN' THEN
        -- Estadísticas para admin
        SELECT 
            (SELECT COUNT(*) FROM users WHERE active = TRUE) as total_users,
            (SELECT COUNT(*) FROM courses WHERE active = TRUE) as total_courses,
            (SELECT COUNT(*) FROM tasks) as total_tasks,
            (SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND active = TRUE) as total_students,
            (SELECT COUNT(*) FROM users WHERE role = 'TEACHER' AND active = TRUE) as total_teachers,
            (SELECT COUNT(*) FROM messages WHERE DATE(sent_at) = CURDATE()) as today_messages;
            
    ELSEIF user_role = 'TEACHER' THEN
        -- Estadísticas para profesor
        SELECT 
            (SELECT COUNT(*) FROM courses WHERE teacher_id = user_id_param AND active = TRUE) as teacher_courses,
            (SELECT COUNT(*) FROM tasks t 
             JOIN courses c ON t.course_id = c.id 
             WHERE c.teacher_id = user_id_param) as teacher_tasks,
            (SELECT COUNT(*) FROM submissions s 
             JOIN tasks t ON s.task_id = t.id 
             JOIN courses c ON t.course_id = c.id 
             WHERE c.teacher_id = user_id_param AND s.grade IS NULL) as pending_submissions;
             
    ELSEIF user_role = 'STUDENT' THEN
        -- Estadísticas para estudiante
        SELECT 
            (SELECT COUNT(*) FROM course_students WHERE student_id = user_id_param) as enrolled_courses,
            (SELECT COUNT(*) FROM tasks t 
             JOIN course_students cs ON t.course_id = cs.course_id 
             WHERE cs.student_id = user_id_param AND t.due_date > NOW()) as upcoming_tasks,
            (SELECT COUNT(*) FROM submissions WHERE student_id = user_id_param) as my_submissions;
    END IF;
END //
DELIMITER ;

-- 🔥 TRIGGERS PARA MANTENER INTEGRIDAD DE DATOS

-- Trigger para actualizar updated_at en users
DELIMITER //
CREATE TRIGGER before_user_update
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END //
DELIMITER ;

-- Trigger para actualizar updated_at en tasks
DELIMITER //
CREATE TRIGGER before_task_update
    BEFORE UPDATE ON tasks
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
END //
DELIMITER ;

-- Trigger para generar código de curso automáticamente
DELIMITER //
CREATE TRIGGER before_course_insert
    BEFORE INSERT ON courses
    FOR EACH ROW
BEGIN
    IF NEW.code IS NULL OR NEW.code = '' THEN
        SET NEW.code = CONCAT('CRS', UNIX_TIMESTAMP());
    END IF;
END //
DELIMITER ;

-- Trigger para auditoría de eliminación de usuarios
DELIMITER //
CREATE TRIGGER after_user_delete
    AFTER DELETE ON users
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (action, description, user_id, ip_address)
    VALUES ('USER_DELETED', CONCAT('Usuario eliminado: ', OLD.email), OLD.id, 'SYSTEM');
END //
DELIMITER ;

-- Mensaje de éxito
SELECT 'Base de datos gestion_tareas creada y configurada exitosamente!' as message;