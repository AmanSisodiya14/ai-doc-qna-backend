# Environment Variables Reference

## Complete List

### Required Variables

| Variable       | Description                 | Example                     |
| -------------- | --------------------------- | --------------------------- |
| `GROQ_API_KEY` | Groq API key for LLM access | `gsk_xxxxxxxxxxxxxxxxxxxxx` |

### Database Configuration

| Variable                     | Description                    | Default                                     | Required |
| ---------------------------- | ------------------------------ | ------------------------------------------- | -------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/aidocqna` | yes      |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username            | `aidocqna`                                  | yes      |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password            | `aidocqna`                                  | yes      |

### Redis Configuration

| Variable                     | Description                  | Default     | Required |
| ---------------------------- | ---------------------------- | ----------- | -------- |
| `SPRING_DATA_REDIS_HOST`     | Redis server hostname        | `localhost` | yes      |
| `SPRING_DATA_REDIS_PORT`     | Redis server port            | `6379`      | yes      |
| `SPRING_DATA_REDIS_PASSWORD` | Redis password (if required) | (empty)     | yes      |

### Security Configuration

| Variable             | Description                       | Default            | Required       |
| -------------------- | --------------------------------- | ------------------ | -------------- |
| `APP_JWT_SECRET`     | JWT signing secret (min 256 bits) | (insecure default) | **Yes (prod)** |
| `APP_JWT_EXPIRATION` | JWT token expiration (ISO 8601)   | `PT24H` (24 hours) | No             |

### AI Service Configuration

| Variable            | Description               | Default                          | Required |
| ------------------- | ------------------------- | -------------------------------- | -------- |
| `GROQ_BASE_URL`     | Groq API endpoint         | `https://api.groq.com/openai/v1` | No       |
| `GROQ_MODEL`        | Groq model name           | `llama-3.3-70b-versatile`        | No       |
| `EMBEDDING_URL`     | Embedding service URL     | `http://localhost:8001`          | No       |
| `TRANSCRIPTION_URL` | Transcription service URL | `http://localhost:8002`          | No       |

### File Storage Configuration

| Variable                  | Description                | Default           | Required |
| ------------------------- | -------------------------- | ----------------- | -------- |
| `APP_STORAGE_PATH`        | File upload directory path | `uploads`         | No       |
| `APP_MAX_FILE_SIZE_BYTES` | Max upload size in bytes   | `52428800` (50MB) | No       |

### RAG Configuration

| Variable                   | Description             | Default | Required |
| -------------------------- | ----------------------- | ------- | -------- |
| `APP_CHUNK_SIZE_TOKENS`    | Chunk size in tokens    | `700`   | No       |
| `APP_CHUNK_OVERLAP_TOKENS` | Chunk overlap in tokens | `100`   | No       |

### Rate Limiting

| Variable                             | Description                      | Default | Required |
| ------------------------------------ | -------------------------------- | ------- | -------- |
| `APP_RATE_LIMIT_REQUESTS_PER_MINUTE` | Max requests per minute per user | `60`    | No       |

---

## Environment-Specific Configurations

### Development (Local)

Create a `.env` file in the `backend/` directory:

```bash
# Required
GROQ_API_KEY=gsk_your_groq_api_key_here

# Database (use local PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aidocqna
SPRING_DATASOURCE_USERNAME=aidocqna
SPRING_DATASOURCE_PASSWORD=aidocqna

# Redis (use local Redis)
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Security (dev only - DO NOT use in production)
APP_JWT_SECRET=dev-secret-key-change-this-in-production-must-be-at-least-256-bits-long

# Python services (local)
EMBEDDING_URL=http://localhost:8001
TRANSCRIPTION_URL=http://localhost:8002
```

### Docker Compose

When using `docker-compose up`, environment variables are set in `docker-compose.yml`:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/aidocqna
  SPRING_DATASOURCE_USERNAME: aidocqna
  SPRING_DATASOURCE_PASSWORD: aidocqna
  SPRING_DATA_REDIS_HOST: redis
  SPRING_DATA_REDIS_PORT: 6379
  GROQ_API_KEY: ${GROQ_API_KEY} # Read from host environment
  EMBEDDING_URL: http://embedding-service:8001
  TRANSCRIPTION_URL: http://transcription-service:8002
```

### Production

**Use secret management systems:**

- **AWS**: Use AWS Secrets Manager or Parameter Store
- **Kubernetes**: Use ConfigMaps (non-sensitive) and Secrets (sensitive)
- **Azure**: Use Azure Key Vault
- **GCP**: Use Secret Manager

**Example Kubernetes Secret:**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: aidocqna-secrets
type: Opaque
stringData:
  GROQ_API_KEY: "gsk_xxxxxxxxxxxxxxxxxxxxx"
  APP_JWT_SECRET: "your-production-256-bit-secret"
  SPRING_DATASOURCE_PASSWORD: "your-db-password"
  SPRING_DATA_REDIS_PASSWORD: "your-redis-password"
```

---

## Security Best Practices

### JWT Secret

**Requirements:**

- Minimum 256 bits (32 characters) for HS256
- Use cryptographically random generation:

```bash
# Generate secure JWT secret
openssl rand -base64 64
```

**Never:**

- Commit secrets to version control
- Use the same secret across environments
- Use predictable values like "secret123"

### Database Credentials

- Use strong, randomly generated passwords
- Rotate credentials regularly (every 90 days)
- Use separate credentials for dev/staging/prod
- Enable SSL/TLS connections in production

### API Keys

- Rotate Groq API key periodically
- Monitor API usage for anomalies
- Use separate keys for dev/prod environments
- Set up billing alerts

---
