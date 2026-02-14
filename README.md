# AI Doc QnA Backend

## Architecture

This backend keeps the existing Spring Boot API surface (JWT auth, file upload, RAG chat, summary, timestamps) and replaces all OpenAI integrations with:

- Groq API for chat and summary generation
- Python `embedding-service` (FastAPI + `sentence-transformers`)
- Python `transcription-service` (FastAPI + `faster-whisper`)
- PostgreSQL for persisted metadata/chunks/embeddings
- Redis for cache (`summary`, `faq`, `embeddings`)

## Integration Boundaries

The Spring application now depends on interfaces:

- `LLMService`
  - `generateAnswer(String context, String question)`
  - `generateSummary(String content)`
- `EmbeddingService`
  - `generateEmbedding(String text)`
- `TranscriptionService`
  - `transcribe(File file)`

Concrete implementations:

- `GroqLLMService`
- `RemoteEmbeddingService`
- `RemoteTranscriptionService`

## Runtime Flow

### File Upload

1. PDF uploads are parsed with PDFBox.
2. Audio/video uploads are sent to `transcription-service`.
3. Content is chunked using existing chunking rules.
4. Each chunk is embedded via `embedding-service`.
5. Chunks and vectors are stored and indexed for similarity search.

### Chat (RAG)

1. User question is embedded via `embedding-service`.
2. Top chunks are selected by cosine similarity.
3. Context + question are sent to Groq.
4. API returns answer + timestamp + confidence in the same response format.

### Summary

1. File chunks are aggregated.
2. Small documents use one-pass summary.
3. Large documents use hierarchical summarization.
4. Result is cached in Redis.

## Configuration

`src/main/resources/application.yml` now uses:

```yaml
groq:
  base-url: https://api.groq.com/openai/v1
  api-key: ${GROQ_API_KEY}
  model: llama3-70b-8192

embedding:
  url: http://embedding-service:8001

transcription:
  url: http://transcription-service:8002
```

No API keys are hardcoded.

## Docker Compose

Run all services:

```bash
docker compose up --build
```

Services started:

- `app` (Spring Boot, port `8080`)
- `postgres` (port `5432`)
- `redis` (port `6379`)
- `embedding-service` (port `8001`)
- `transcription-service` (port `8002`)

Required environment variable:

- `GROQ_API_KEY`

## CI

GitHub Actions workflow: `.github/workflows/ci.yml`

- Runs backend tests with Maven
- Compiles Python microservice code (`compileall`) for both FastAPI services
