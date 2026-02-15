# API Endpoints Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

All endpoints except `/auth/register` and `/auth/login` require JWT authentication:

```http
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Register User

**Endpoint:** `POST /api/auth/register`

**Request:**

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Validation:**

- `name`: Required, non-blank
- `email`: Required, valid email format
- `password`: Required, 3-128 characters

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "john.doe@example.com",
    "name": "John Doe"
  },
  "message": "Operation successful"
}
```

**Error Responses:**

- `400 Bad Request`: Invalid email or password too short
- `409 Conflict`: Email already registered

---

### Login

**Endpoint:** `POST /api/auth/login`

**Request:**

```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "john.doe@example.com",
    "name": "John Doe"
  },
  "message": "Operation successful"
}
```

**Error Responses:**

- `401 Unauthorized`: Invalid credentials

---

## File Management Endpoints

### Upload File

**Endpoint:** `POST /api/files/upload`

**Headers:**

```http
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Request:**

```
file: <binary-file>
```

**Accepted Formats:**

- **PDF**: `.pdf`
- **Audio**: `.mp3`, `.wav`, `.m4a`, `.aac`, `.flac`
- **Video**: `.mp4`, `.webm`, `.ogg`, `.avi`, `.mov`, `.mkv`

**Max File Size:** 50 MB

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 123,
    "fileName": "presentation.pdf",
    "fileType": "pdf",
    "status": "PROCESSED",
    "storedFileName": "550e8400-e29b-41d4-a716-446655440000-presentation.pdf"
  },
  "message": "Operation successful"
}
```

**Processing Steps:**

1. File stored to local filesystem
2. PDF → text extraction (PDFBox)
3. Audio/Video → transcription (Faster Whisper)
4. Content chunked (700 tokens, 100 overlap)
5. Chunks embedded (BGE-small-en)
6. Vectors indexed in database

**Error Responses:**

- `400 Bad Request`: Unsupported file type or file too large
- `401 Unauthorized`: Missing or invalid token
- `500 Internal Server Error`: Processing failure

---

### List Files

**Endpoint:** `GET /api/files?page=0&size=10`

**Headers:**

```http
Authorization: Bearer <token>
```

**Query Parameters:**

- `page` (optional): Page number, default `0`
- `size` (optional): Items per page, default `10`, max `100`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "fileName": "presentation.pdf",
        "fileType": "pdf",
        "uploadDate": "2026-02-15T10:30:00Z"
      },
      {
        "id": 124,
        "fileName": "lecture.mp4",
        "fileType": "mp4",
        "uploadDate": "2026-02-15T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  },
  "message": "Operation successful"
}
```

---

### Get File Summary

**Endpoint:** `GET /api/files/{fileId}/summary`

**Headers:**

```http
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "summary": "This document discusses the implementation of RAG systems using vector databases and LLMs. Key topics include embedding generation, semantic search, and context retrieval for improving AI answer accuracy..."
  },
  "message": "Operation successful"
}
```

**Caching:** Summaries are cached in Redis for 24 hours

**Error Responses:**

- `404 Not Found`: File not found or not owned by user

---

### Search Timestamps

**Endpoint:** `GET /api/files/{fileId}/timestamps?query=introduction`

**Headers:**

```http
Authorization: Bearer <token>
```

**Query Parameters:**

- `query` (required): Search term or topic

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "startTime": 45000,
    "endTime": 78000,
    "content": "In the introduction, we'll cover the basics of AI-powered Q&A systems and how they leverage retrieval-augmented generation..."
  },
  "message": "Operation successful"
}
```

**Note:** Times are in milliseconds. Only applicable to audio/video files.

**Error Responses:**

- `400 Bad Request`: Query parameter missing
- `404 Not Found`: No matching chunk found or file is PDF

---

## Chat Endpoint

### Ask Question

**Endpoint:** `POST /api/chat`

**Headers:**

```http
Authorization: Bearer <token>
Content-Type: application/json
```

**Request:**

```json
{
  "fileId": 123,
  "message": "What are the main benefits of RAG systems?"
}
```

**Validation:**

- `fileId`: Required
- `message`: Required, non-blank

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "answer": "RAG (Retrieval-Augmented Generation) systems provide several key benefits:\n\n1. **Reduced Hallucinations**: By grounding responses in actual document content, RAG systems significantly reduce the likelihood of generating false information.\n\n2. **Cost-Effective**: More economical than fine-tuning large models, as it leverages existing LLMs with dynamic context.\n\n3. **Up-to-date Information**: Can incorporate the latest documents without retraining the model.\n\n4. **Source Attribution**: Enables tracking which parts of documents contributed to answers.",
    "startTime": 120000,
    "endTime": 185000
  },
  "message": "Operation successful"
}
```

**Response Fields:**

- `answer`: AI-generated response based on retrieved context
- `startTime`: Start timestamp in milliseconds (NULL for PDFs)
- `endTime`: End timestamp in milliseconds (NULL for PDFs)

**RAG Pipeline:**

1. User question embedded via embedding service
2. Top-3 most similar chunks retrieved (cosine similarity)
3. Chunk content + question sent to Groq LLM
4. AI generates contextual answer
5. Best matching chunk's timestamps returned

**Caching:** Responses cached in Redis with key `fileId:question`

**Error Responses:**

- `400 Bad Request`: Invalid request body
- `404 Not Found`: File not found or no indexed chunks
- `500 Internal Server Error`: LLM service failure

---

## Media Streaming

**Endpoint:** `GET /media/{storedFileName}`

**Example:**

```
http://localhost:8080/media/550e8400-e29b-41d4-a716-446655440000-lecture.mp4
```

**Description:**
Serves uploaded media files for playback. Files are stored locally in the `uploads/` directory and exposed via Spring Boot's static resource handler.

**Security Note:**
Currently no authentication on media endpoints. For production, implement signed URLs or JWT validation.

---

## Rate Limiting

All endpoints are rate-limited to **60 requests per minute** per user (based on email from JWT).

**Rate Limit Headers:**

```http
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1645012345
```

**Error Response (429 Too Many Requests):**

```json
{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "timestamp": "2026-02-15T10:30:00Z"
}
```

---

## Error Response Format

All error responses follow this structure:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-02-15T10:30:00Z"
}
```

**Common HTTP Status Codes:**

- `200 OK`: Success
- `400 Bad Request`: Invalid input
- `401 Unauthorized`: Missing or invalid JWT
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error
