CREATE DATABASE aufgabe DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;

USE aufgabe;

CREATE TABLE task_entry (
  id VARCHAR(128) NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  description TEXT NOT NULL,
  PRIMARY KEY(id)
);