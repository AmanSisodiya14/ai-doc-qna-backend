# Database Schema

## Overview

The application uses PostgreSQL to store user data, file metadata, and vectorized content chunks for semantic search.

## Tables

### Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL
);
```

**Columns:**

- `id`: Auto-incrementing primary key
- `name`: User's full name
- `email`: Unique email address (used for login)
- `password`: BCrypt-hashed password
- `role`: User role enum (`USER`, `ADMIN`)
- `created_at`: Account creation timestamp

### Files Table

```sql
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    file_name VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    extracted_text TEXT,
    transcript TEXT
);
```

**Columns:**

- `id`: Auto-incrementing primary key
- `user_id`: Foreign key to users table
- `file_name`: Original filename from upload
- `file_type`: File extension (`pdf`, `mp3`, `mp4`, etc.)
- `storage_path`: Local filesystem path to stored file
- `upload_date`: Upload timestamp
- `extracted_text`: For PDFs, the extracted text; for media, same as transcript
- `transcript`: For audio/video files, the full Whisper transcription

### Chunks Table

```sql
CREATE TABLE chunks (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES files(id),
    content TEXT NOT NULL,
    embedding TEXT,  -- JSON array of doubles
    start_time BIGINT,  -- milliseconds
    end_time BIGINT,    -- milliseconds
    chunk_order INTEGER NOT NULL
);
```

**Columns:**

- `id`: Auto-incrementing primary key
- `file_id`: Foreign key to files table
- `content`: Text content of this chunk
- `embedding`: JSON-serialized array of 384 doubles (BGE-small-en vector)
- `start_time`: Start timestamp in milliseconds (NULL for PDFs)
- `end_time`: End timestamp in milliseconds (NULL for PDFs)
- `chunk_order`: Sequential order of chunk in original document (0-based)

## Relationships

```
users (1) ──── (N) files
files (1) ──── (N) chunks
```

- One user can have multiple files
- One file is split into multiple chunks
- Cascade delete: Deleting a user deletes their files and chunks
- Cascade delete: Deleting a file deletes its chunks

## Indexes

**Recommended Production Indexes:**

```sql
-- Primary lookups
CREATE INDEX idx_files_user_id ON files(user_id);
CREATE INDEX idx_chunks_file_id ON chunks(file_id);

-- Auth lookups
CREATE INDEX idx_users_email ON users(email);

-- Chunk ordering
CREATE INDEX idx_chunks_file_order ON chunks(file_id, chunk_order);
```

## Storage Estimates

- **Users**: ~500 bytes/user
- **Files**: ~1-2 KB/file (metadata only)
- **Chunks**: ~2-4 KB/chunk (384 doubles + text content)

**Example**: A 50-page PDF might generate 100 chunks = ~300 KB in database

## Data Flow

1. **User Registration**: Insert into `users` with hashed password
2. **File Upload**:
   - Insert into `files` with metadata
   - Extract/transcribe content
   - Split into chunks (700 tokens, 100 overlap)
   - Generate embeddings via Python service
   - Insert chunks with embeddings into `chunks`
3. **Query**:
   - Embed user question
   - Compute cosine similarity across all chunks for file
   - Retrieve top-K chunks
   - Pass to LLM for answer generation

## Schema Evolution

The schema is managed by Spring Data JPA with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # Auto-migrate schema
```

**Note**: For production, consider using Flyway or Liquibase for versioned migrations.
