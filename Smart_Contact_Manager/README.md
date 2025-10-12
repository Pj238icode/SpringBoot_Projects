# Contact Manager Spring Boot Application

A full-featured **Contact Manager** application built with **Spring Boot**, **Spring Data JPA**, and **MySQL**, with support for JWT authentication, Google OAuth login, file uploads, AWS S3 integration, Stripe payments, and real-time sync.

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

- **User Authentication & Authorization**  
  - JWT-based authentication  
  - Google OAuth2 login  

- **Contact Management**  
  - Create, read, update, and delete contacts  
  - Merge duplicate contacts automatically  
  - Import contacts from CSV or Excel files  
  - Export contacts to CSV or PDF  
  - Birthday reminders via email notifications  

- **File Uploads & Media Management**  
  - Upload and manage contact photos  
  - AWS S3 integration for secure storage  
  - Default placeholder image for contacts without photos  

- **Real-Time Sync Across Devices**  
  - Contacts automatically updated on multiple devices using polling  
  - Instant notification of changes  

- **Payments & Credits**  
  - Stripe payment gateway integration to purchase/add credits  
  - Track and manage user credit balance  

- **Security & Access Control**  
  - Configurable CORS policies  
  - Secure password storage and reset  
  - Email notifications for account and contact updates  

- **Performance & Usability**  
  - Pagination and filtering of contacts  
  - Bulk import/export for efficient data management  

---

## Tech Stack

<p align="center">
  <img src="https://img.icons8.com/color/48/000000/spring-logo.png" alt="Spring Boot" width="50" height="50"/> 
  <img src="https://img.icons8.com/color/48/000000/java-coffee-cup-logo.png" alt="Java" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/mysql-logo.png" alt="MySQL" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/hibernate.png" alt="Hibernate" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/oauth.png" alt="OAuth2" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/amazon-s3.png" alt="AWS S3" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/maven.png" alt="Maven" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/docker.png" alt="Docker" width="50" height="50"/>
  <img src="https://img.icons8.com/color/48/000000/stripe.png" alt="Stripe" width="50" height="50"/>
</p>

---

## Getting Started

### Prerequisites

- **Java 17+** ![Java](https://img.icons8.com/color/24/000000/java-coffee-cup-logo.png)  
- **Maven 3+** ![Maven](https://img.icons8.com/color/24/000000/maven.png)  
- **MySQL database** ![MySQL](https://img.icons8.com/color/24/000000/mysql-logo.png)  
- **Docker (optional)** ![Docker](https://img.icons8.com/color/24/000000/docker.png)  

### Clone the Repository

```bash
git clone https://github.com/Pj238icode/SpringBoot-.git
cd SpringBoot-/Smart_Contact_Manager
