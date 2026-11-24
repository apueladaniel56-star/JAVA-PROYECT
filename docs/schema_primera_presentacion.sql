-- Script de creación de la base de datos para el Sistema de Gestión de Préstamos para Biblioteca.
-- Ejecutar este script en MySQL (Workbench, consola o phpMyAdmin) antes de correr el programa.

CREATE DATABASE IF NOT EXISTS biblioteca_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE biblioteca_db;

CREATE TABLE IF NOT EXISTS libro (
    codigo              INT PRIMARY KEY,
    titulo              VARCHAR(150) NOT NULL,
    autor               VARCHAR(100) NOT NULL,
    categoria           VARCHAR(60)  NOT NULL,
    copias_totales      INT NOT NULL,
    copias_disponibles  INT NOT NULL
);

-- Nota: el catálogo de ejemplo (códigos 101-105) se inserta automáticamente desde el
-- programa Java la primera vez que se ejecuta si esta tabla está vacía, para que no
-- haga falta poblarla a mano.
