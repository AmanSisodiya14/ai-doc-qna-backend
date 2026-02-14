import os
import tempfile

from fastapi import FastAPI, File, HTTPException, UploadFile
from faster_whisper import WhisperModel

app = FastAPI(title="transcription-service", version="1.0.0")
model = WhisperModel("base", device="cpu", compute_type="int8")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/transcribe")
async def transcribe(file: UploadFile = File(...)) -> dict[str, list[dict[str, float | str]]]:
    if file.filename is None or file.filename.strip() == "":
        raise HTTPException(status_code=400, detail="Audio file is required")

    extension = os.path.splitext(file.filename)[1] or ".bin"
    with tempfile.NamedTemporaryFile(delete=False, suffix=extension) as temp_file:
        temp_file.write(await file.read())
        temp_path = temp_file.name

    try:
        segments, _ = model.transcribe(temp_path, vad_filter=True)
        payload: list[dict[str, float | str]] = []
        for segment in segments:
            payload.append(
                {
                    "text": segment.text.strip(),
                    "start": float(segment.start),
                    "end": float(segment.end),
                }
            )
        return {"segments": payload}
    except Exception as exc:  # noqa: BLE001
        raise HTTPException(status_code=500, detail=f"Failed to transcribe audio: {exc}") from exc
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)
