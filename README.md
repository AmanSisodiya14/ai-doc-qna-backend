# AI-Powered Document & Multimedia Q&A - Backend

The backend for the AI-Powered Document Q&A application, built with Spring Boot and Python microservices. It handles file processing (PDF, audio, video), RAG (Retrieval-Augmented Generation) pipeline, and serves the REST API.

## 🛠 Tech Stack

### Core Backend

- **Java 17**
- **Spring Boot 4.0.2**
- **PostgreSQL 16** (Metadata, Vectors)
- **Redis 7** (Caching)
- **Spring Security + JWT** (Authentication)
- **Apache PDFBox 3.0.2** (PDF Processing)

### AI Microservices (Python)

- **Embedding Service**: FastAPI + BGE-small-en (Sentence Transformers)
- **Transcription Service**: FastAPI + Faster Whisper

### AI Integration

- **Groq API**: LLaMA 3.3 70B (LLM for Q&A)

---

**RAG Pipeline:**

- **PDF**: Extract text → Chunk → Embed → Store → Search → LLM Answer
- **Audio/Video**: Transcribe → Chunk (Time-based) → Embed → Store → Search → LLM Answer

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - [Download JDK 17](https://adoptium.net/)
- **Python 3.11+** - [Download Python](https://www.python.org/downloads/)
- **PostgreSQL 16+** - [Download PostgreSQL](https://www.postgresql.org/download/)
- **Redis 7+** - [Download Redis](https://redis.io/download/)
- **Maven 3.9+** - (or use included Maven wrapper)

---

## ⚙️ Manual Setup

### Step 1: Configure Backend (application.yml)

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aidocqna
    username: aidocqna
    password: aidocqna
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  data:
    redis:
      host: localhost
      port: 6379
      password: # Leave empty if no password set

app:
  groq:
    api-key: your-groq-api-key-here # Set this as environment variable or directly here for dev
    model: llama-3.3-70b-versatile
  jwt:
    secret: your-secure-256-bit-secret-change-this-in-production
  embedding:
    url: http://localhost:8001
  transcription:
    url: http://localhost:8002
  storage-path: uploads
```

### Step 2: Start Python Services

**Embedding Service:**

```bash
cd embedding-service

# Windows Setup & Run
py -m venv .venv
py .\.venv\Scripts\Activate.ps1
py -m pip install --upgrade pip
py -m pip install -r requirements.txt
py -m uvicorn app.main:app --host 0.0.0.0 --port 8001
```

**Transcription Service:**
Open a new terminal.

```bash
cd transcription-service

# Windows Setup & Run
py -m venv .venv
py .\.venv\Scripts\Activate.ps1
py -m pip install --upgrade pip
py -m pip install -r requirements.txt
py -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

### Step 3: Start Spring Boot Backend

Open a new terminal in the `backend` root.

```bash
# Set API Key (Unix/Mac)
export GROQ_API_KEY="your-groq-api-key"

# Set API Key (Windows PowerShell)
$env:GROQ_API_KEY="your-groq-api-key"

# Run using Maven Wrapper
./mvnw spring-boot:run
```

The backend API will be available at: **http://localhost:8080**

---

## 🔑 Environment Variables

| Variable                     | Description       | Default                                     | Required      |
| ---------------------------- | ----------------- | ------------------------------------------- | ------------- |
| `GROQ_API_KEY`               | Groq API Key      | -                                           | **Yes**       |
| `SPRING_DATASOURCE_URL`      | DB Connection URL | `jdbc:postgresql://localhost:5432/aidocqna` | **Yes**       |
| `SPRING_DATASOURCE_USERNAME` | DB Username       | `aidocqna`                                  | **Yes**       |
| `SPRING_DATASOURCE_PASSWORD` | DB Password       | `aidocqna`                                  | **Yes**       |
| `SPRING_DATA_REDIS_HOST`     | Redis Host        | `localhost`                                 | **Yes**       |
| `APP_JWT_SECRET`             | JWT Secret Key    | `dev-secret...`                             | **Prod Only** |

See `../docs/environment-variables.md` for the full list.

---

## 📡 API Endpoints

| Method | Endpoint                     | Description                     |
| ------ | ---------------------------- | ------------------------------- |
| POST   | `/api/auth/register`         | User registration               |
| POST   | `/api/auth/login`            | User login                      |
| POST   | `/api/files/upload`          | Upload PDF/Audio/Video          |
| GET    | `/api/files`                 | List files                      |
| POST   | `/api/chat`                  | Chat with complete file context |
| GET    | `/api/files/{id}/timestamps` | Search video/audio timestamps   |

See `../docs/api-endpoints.md` for detailed documentation.
