from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from db import CUESTIONARIOS, NUTRIENTES, NUTRIENTES_CUESTIONARIOS
from api_rest import get_db
from pydantic import BaseModel
from typing import List

router = APIRouter()

class NutrientesRequest(BaseModel):
    id_user: int
    nutrientes: List[int]

@router.post("/guardar_nutrientes")
def guardar_nutrientes(request: NutrientesRequest, db: Session = Depends(get_db)):

    cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == request.id_user).first()

    cuestionario = CUESTIONARIOS(
        IDUser=request.id_user
    )
    db.add(cuestionario)
    db.commit()
    db.refresh(cuestionario)

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

    db.commit()

    return {
        "message": "Nutrientes guardados correctamente",
        "idCuestionario": cuestionario.IDCuestionario
    }