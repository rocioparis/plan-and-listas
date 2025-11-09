from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from db import CUESTIONARIOS, ESTADOSCUES, NUTRIENTES, NUTRIENTES_CUESTIONARIOS
from api_rest import get_db
from pydantic import BaseModel
from typing import List

router = APIRouter()

class NutrientesRequest(BaseModel):
    id_user: int
    id_estado: int
    nutrientes: List[int]

@router.post("/guardar_nutrientes")
def guardar_nutrientes(request: NutrientesRequest, db: Session = Depends(get_db)):
    estado_permitido = db.query(ESTADOSCUES).filter(ESTADOSCUES.IDEstadoT == request.id_estado).first()
    if not estado_permitido:
        raise HTTPException(status_code=400, detail="Estado de cuestionario inválido")

    cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == request.id_user).first()

    if not cuestionario:
        cuestionario = CUESTIONARIOS(
            IDUser=request.id_user,
            IDEstadoT=estado_permitido.IDEstadoT
        )
        db.add(cuestionario)
        db.commit()
        db.refresh(cuestionario)
    else:
        cuestionario.IDEstadoT = estado_permitido.IDEstadoT
        db.commit()

    for id_nutriente in request.nutrientes:
        nutriente_existente = db.query(NUTRIENTES).filter(NUTRIENTES.IDNutriente == id_nutriente).first()
        if not nutriente_existente:
            continue
        nuevo_nutriente = NUTRIENTES_CUESTIONARIOS(
            IDCuestionario=cuestionario.IDCuestionario,
            IDNutriente=id_nutriente
        )
        db.add(nuevo_nutriente)
        db.flush()

    print("Nutrientes a insertar:", [id_nutriente for id_nutriente in request.nutrientes])

    for nutriente in db.query(NUTRIENTES_CUESTIONARIOS).filter(NUTRIENTES_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario):
        print(nutriente.IDNutriente)

    db.commit()

    return {
        "message": "Nutrientes guardados y estado actualizado correctamente",
        "idCuestionario": cuestionario.IDCuestionario
    }