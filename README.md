# 🚀 DevHub – Developer Blog & Portfolio Platform

## 📌 Project Overview

**DevHub** is a full-stack web application for developers to:

- ✍️ Create & manage blog posts.
- 🧠 Showcase portfolio projects.
- 💬 Comment on other developers’ blogs.
- 👤 Manage personal profiles (bio, image, etc.).

🔐 It includes secure login with **JWT Authentication** and **role-based access control**:

| Role       | Permissions                                      |
|------------|--------------------------------------------------|
| ADMIN      | Manage users, blog posts, and moderate comments |
| DEVELOPER  | Create/manage own blog posts, projects, and comments |

---

## 🛠 Tech Stack

| Layer      | Technology                                      |
|------------|--------------------------------------------------|
| Backend    | Spring Boot, Spring Web, Spring Security, Spring Data JPA |
| Frontend   | HTML, CSS, JavaScript                           |
| Database   | MySQL / PostgreSQL                              |
| Security   | JWT (JSON Web Token) Authentication             |

---

## 📦 Core Modules & Relationships

### 👤 1. User

| Field           | Type     | Description                   |
|----------------|----------|-------------------------------|
| id             | Long     | Primary key                   |
| username       | String   | Unique username               |
| email          | String   | Unique email address          |
| password       | String   | Encrypted password            |
| bio            | String   | Short profile bio             |
| role           | Enum     | Role: ADMIN or DEVELOPER      |

🔗 **Relationships**:
- One-to-Many → BlogPost
- One-to-Many → Project
- One-to-Many → Comment

---

### 📝 2. BlogPost

| Field         | Type       | Description                    |
|--------------|------------|--------------------------------|
| id           | Long       | Primary key                    |
| title        | String     | Title of the blog              |
| content      | Text       | Blog content                   |
| coverImageUrl| String     | URL of the blog image/banner   |
| tags         | String[]   | Comma-separated tags           |
| createdAt    | DateTime   | Blog creation timestamp        |

🔗 **Relationships**:
- Many-to-One → User (Author)
- One-to-Many → Comment

---

### 💼 3. Project

| Field          | Type       | Description                         |
|---------------|------------|-------------------------------------|
| id            | Long       | Primary key                         |
| title         | String     | Project title                       |
| description   | String     | Short project description           |
| githubUrl     | String     | GitHub repository URL               |
| liveDemoUrl   | String     | Live demo URL (optional)            |
| techStack     | String[]   | Technologies used (e.g. Java, React)|
| completionDate| Date       | Project completion date             |

🔗 **Relationship**:
- Many-to-One → User (Project owner)

---

### 💬 4. Comment

| Field       | Type     | Description                   |
|------------|----------|-------------------------------|
| id         | Long     | Primary key                   |
| content    | String   | Text of the comment           |
| commentedAt| DateTime | Timestamp of the comment      |

🔗 **Relationships**:
- Many-to-One → BlogPost
- Many-to-One → User

---

## 🔗 Entity Relationship (ER) Diagram

```plaintext
+--------+       1     N     +----------+
|  User  |-------------------| BlogPost |
+--------+                   +----------+
     |                             |
     | 1                         N |
     |                             V
     |                        +--------+
     |                        |Comment |
     |                        +--------+
     |                             ^
     | 1                         N |
     |                             |
     |                             |
     |                          +--------+
     +------------------------->| Project|
                                +--------+
Relationship	        Type
User → BlogPost	One-to-Many
User → Project	     One-to-Many
User → Comment	     One-to-Many
BlogPost → Comment	One-to-Many

🔒 Authentication & Authorization
Feature	                   Description
Spring Security      Role-based access for ADMIN and DEVELOPER roles
JWT Token	Stateless, secure login mechanism
Access Control	Only  logged-in users can create/edit content
Protected Routes	 JWT required in Authorization header

🏁 Project Setup
🔧 Backend Setup

Clone the repository:
bash

git clone https://github.com/your-username/devhub.git
cd devhub
Configure Database (MySQL or PostgreSQL):
Update application.properties or application.yml:

properties

spring.datasource.url=jdbc:mysql://localhost:3306/devhub
spring.datasource.username=root
spring.datasource.password=your_password
Run the Application:

bash
./mvnw spring-boot:run
🌐 Frontend Setup (Optional)
Use HTML, CSS, JavaScript for static pages.

Or integrate with a frontend framework (React, Angular, etc.) for better UX.

🧪 Future Enhancements
Feature                   	Status
Blog post Like/Bookmark	     Planned
Tag filtering/search	     Planned
Markdown support for blogs	Planned
Admin dashboard UI	          Planned
Project image uploads     	Planned

🤝 Contribution Guidelines
Step	                       Description
Fork Repository	Create your copy of the repo
Create Branch	     Use meaningful branch name
Commit Changes	     Use clear commit messages
Open Pull Request	Describe changes & link to issue (if any)

📄 License
This project is currently not licensed. You are free to fork and use for learning or personal development.

```
<pre>
🙋‍♂️ Contact
Email	satishpakalapati65@example.com
GitHub	Satish-970
</pre>
