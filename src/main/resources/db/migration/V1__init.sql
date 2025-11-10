-- Charset recomendado
-- (opcional si tu servidor ya usa utf8mb4)
-- SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- USERS (para auth)
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;

-- COURSES / WEEKS / MATERIALS
CREATE TABLE IF NOT EXISTS courses (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       name VARCHAR(255) NOT NULL,
    color VARCHAR(20) NULL,
    description TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS course_weeks (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            course_id BIGINT NOT NULL,
                                            number INT NOT NULL,
                                            title VARCHAR(255) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_week_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS materials (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         course_id BIGINT NOT NULL,
                                         week_id BIGINT NULL,
                                         title VARCHAR(255) NOT NULL,
    type VARCHAR(16) NOT NULL,            -- DOC|IMG|LINK|TEXT
    info_text TEXT NULL,
    url TEXT NULL,
    mime VARCHAR(255) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_mat_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_mat_week   FOREIGN KEY (week_id)   REFERENCES course_weeks(id) ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- ACTIVITIES & REMINDERS
CREATE TABLE IF NOT EXISTS activities (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          course_id BIGINT NOT NULL,
                                          description TEXT NOT NULL,
                                          due_date DATE NULL,
                                          due_time TIME NULL,
                                          notify BOOLEAN NOT NULL DEFAULT FALSE,
                                          status VARCHAR(16) NOT NULL DEFAULT 'PENDING',  -- PENDING|DONE|MISSED
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_act_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reminders (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         activity_id BIGINT NOT NULL,
                                         remind_at DATETIME NOT NULL,
                                         CONSTRAINT fk_rem_act FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- SCHEDULE (horario)
CREATE TABLE IF NOT EXISTS schedule_items (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              course_id BIGINT NOT NULL,
                                              day_of_week INT NOT NULL,             -- 1..7
                                              start_time TIME NOT NULL,
                                              end_time TIME NOT NULL,
                                              room VARCHAR(100) NULL,
    color VARCHAR(20) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_sched_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- GRADES (con peso porcentual)
CREATE TABLE IF NOT EXISTS grades (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      course_id BIGINT NOT NULL,
                                      activity_id BIGINT NULL,
                                      score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    weight_percent DECIMAL(5,2) NULL,     -- 0..100, null si no pondera
    graded_at DATE NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- NOTES (texto)
CREATE TABLE IF NOT EXISTS notes (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     course_id BIGINT NULL,
                                     title VARCHAR(255) NOT NULL,
    content TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_note_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- FINANCE
CREATE TABLE IF NOT EXISTS finance_entries (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               entry_date DATE NOT NULL,
                                               type VARCHAR(16) NOT NULL,            -- INCOME|EXPENSE
    amount DECIMAL(12,2) NOT NULL,
    category VARCHAR(100) NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;

-- HEALTH (rutinas y sesiones)
CREATE TABLE IF NOT EXISTS health_workout_routines (
                                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                       name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS health_workout_sessions (
                                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                       routine_id BIGINT NULL,
                                                       title VARCHAR(255) NULL,
    session_date DATE NOT NULL,
    duration_minutes INT NULL,
    calories_burned INT NULL,
    notes TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_session_routine FOREIGN KEY (routine_id) REFERENCES health_workout_routines(id) ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- NUTRITION
CREATE TABLE IF NOT EXISTS nutrition_meals (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               meal_date DATE NOT NULL,
                                               meal_type VARCHAR(16) NOT NULL,       -- BREAKFAST|LUNCH|DINNER|SNACK
    calories INT NULL,
    protein_g DECIMAL(6,2) NULL,
    carbs_g DECIMAL(6,2) NULL,
    fat_g DECIMAL(6,2) NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;

-- PERSONAL TASKS
CREATE TABLE IF NOT EXISTS personal_tasks (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              description VARCHAR(500) NOT NULL,
    due_date DATE NULL,
    due_time TIME NULL,
    notify BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',   -- PENDING|DONE|MISSED
    created_at DATETIME NULL,
    updated_at DATETIME NULL
    ) ENGINE=InnoDB;
