CREATE DATABASE aufgabe;

USE aufgabe;

CREATE TABLE task_entry (
  id VARCHAR(128) NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  description TEXT NOT NULL,
  PRIMARY KEY(id)
);