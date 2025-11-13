from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from db import CUESTIONARIOS, METAS, METAS_CUESTIONARIOS
from api_rest import get_db
from pydantic import BaseModel
from typing import List

router = APIRouter()

class MetasRequest(BaseModel):
    id_user: int
    metas: List[int]

@router.post("/guardar_metas")
def guardar_metas(request: MetasRequest, db: Session = Depends(get_db)):

    cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == request.id_user).first()

    cuestionario = CUESTIONARIOS(
        IDUser=request.id_user
    )
    db.add(cuestionario)
    db.commit()
    db.refresh(cuestionario)

    for id_meta in request.metas:
        meta_existente = db.query(METAS).filter(METAS.IDMeta == id_meta).first()
        if not meta_existente:
            continue 
        nueva_meta = METAS_CUESTIONARIOS(
            IDCuestionario=cuestionario.IDCuestionario,
            IDMeta=id_meta
        )
        db.add(nueva_meta)
        db.flush()

    db.commit()

    return {
        "message": "Metas guardadas correctamente",
        "idCuestionario": cuestionario.IDCuestionario
    }