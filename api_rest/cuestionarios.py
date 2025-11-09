from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from db import CUESTIONARIOS, ESTADOSCUES, USERS
from api_rest import get_db

router = APIRouter()

class CuestionarioRequest(BaseModel):
    id_user: int
    id_estado: int

@router.post("/guardar_cuestionario")
def guardar_cuestionario(request: CuestionarioRequest, db: Session = Depends(get_db)):
    print(f"Llega request al backend: {request}")

    usuario = db.query(USERS).filter(USERS.IDUser == request.id_user).first()
    if not usuario:
        raise HTTPException(status_code=404, detail="Persona usuaria no encontrada")

    estado = db.query(ESTADOSCUES).filter(ESTADOSCUES.IDEstadoT == request.id_estado).first()
    if not estado:
        raise HTTPException(status_code=400, detail="Estado inválido")

    cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == request.id_user).first()

    if cuestionario:
        cuestionario.IDEstadoT = estado.IDEstadoT
        db.commit()
        mensaje = "Cuestionario actualizado correctamente"
    else:
        nuevo_cuestionario = CUESTIONARIOS(
            IDUser = request.id_user,
            IDEstadoT = estado.IDEstadoT
        )
        db.add(nuevo_cuestionario)
        db.commit()
        mensaje = "Cuestionario guardado correctamente"

    return {
        "message": mensaje,
        "id_user": request.id_user,
        "id_estado": request.id_estado
    }