from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI(title="embedding-service", version="1.0.0")
model = SentenceTransformer("BAAI/bge-small-en")


class EmbedRequest(BaseModel):
    text: str


class EmbedResponse(BaseModel):
    embedding: list[float]


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest) -> EmbedResponse:
    text = request.text.strip()
    if not text:
        raise HTTPException(status_code=400, detail="Text for embedding must not be empty")

    vector = model.encode(text, normalize_embeddings=True).tolist()
    return EmbedResponse(embedding=[float(value) for value in vector])
