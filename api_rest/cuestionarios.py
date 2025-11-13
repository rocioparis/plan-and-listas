from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from db import CUESTIONARIOS, USERS
from api_rest.api_rest import get_db

router = APIRouter()

class CuestionarioRequest(BaseModel):
    id_user: int

@router.post("/guardar_cuestionario")
def guardar_cuestionario(request: CuestionarioRequest, db: Session = Depends(get_db)):
    print(f"Llega request al backend: {request}")

    usuario = db.query(USERS).filter(USERS.IDUser == request.id_user).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Persona usuaria no encontrada")

    nuevo_cuestionario = CUESTIONARIOS(
        IDUser = request.id_user
    )
    db.add(nuevo_cuestionario)
    db.commit()
    mensaje = "Cuestionario guardado correctamente"

    return {
        "message": mensaje,
        "id_user": request.id_user
    }