# Contact Manager Spring Boot Application

A full-featured **Contact Manager** application built with **Spring Boot**, **Spring Data JPA**, and **MySQL**, with support for JWT authentication, Google OAuth login, file uploads, and AWS S3 integration.

---

## Table of Contents

- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Getting Started](#getting-started)  
- [Environment Variables](#environment-variables)  
- [Running with Docker](#running-with-docker)  
- [API Endpoints](#api-endpoints)  
- [Contributing](#contributing)  
- [License](#license)  

---

## Features

- User registration and authentication using **JWT**  
- Login via **Google OAuth2**  
- CRUD operations for contacts  
- File upload support (images, PDFs, CSVs)  
- AWS S3 integration for storing uploaded files  
- Stripe payment gateway integration  
- Secure CORS configuration  

---

## Tech Stack

- **Backend:** Spring Boot 3.x  
- **Database:** MySQL / MariaDB  
- **ORM:** Spring Data JPA / Hibernate  
- **Authentication:** JWT, Google OAuth2  
- **Cloud:** AWS S3  
- **Build & Dependency:** Maven  
- **Containerization:** Docker  

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3+
- MySQL database
- Docker (optional)

### Clone the repository

```bash
git clone https://github.com/Pj238icode/SpringBoot-.git
cd SpringBoot-/Smart_Contact_Manager
