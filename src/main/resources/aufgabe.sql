CREATE DATABASE aufgabe DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;

USE aufgabe;

CREATE TABLE task_entry (
    id VARCHAR(128) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    taskId VARCHAR(128) NOT NULL,
    updatedAt TIMESTAMP NOT NULL,
    syncedAt TIMESTAMP NULL,
    PRIMARY KEY(id)
);

CREATE TABLE task (
    id VARCHAR(128) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    description TEXT NOT NULL,
    updatedAt TIMESTAMP NOT NULL,
    syncedAt TIMESTAMP NULL,
    PRIMARY KEY(id)
);

CREATE TABLE button (
    id VARCHAR(128) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    ioPin VARCHAR(128) NOT NULL,
    device VARCHAR(128) NOT NULL,
    taskId VARCHAR(128) NOT NULL,
    removedAt TIMESTAMP NULL,
    updatedAt TIMESTAMP NOT NULL,
    syncedAt TIMESTAMP NULL,
    PRIMARY KEY(id)
);

CREATE TABLE sync_entry (
    id VARCHAR(128) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    startedAt TIMESTAMP NULL,
    completedAt TIMESTAMP NULL,
    numberOfRecordsSynced INT NULL,
    recordsSynced TEXT NULL,
    PRIMARY KEY(id)
);