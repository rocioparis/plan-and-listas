from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from db import CUESTIONARIOS, ESTADOSCUES, METAS, METAS_CUESTIONARIOS
from api_rest import get_db
from pydantic import BaseModel
from typing import List

router = APIRouter()

class MetasRequest(BaseModel):
    id_user: int
    id_estado: int
    metas: List[int]

@router.post("/guardar_metas")
def guardar_metas(request: MetasRequest, db: Session = Depends(get_db)):
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

    print("Metas a insertar:", [id_meta for id_meta in request.metas])

    for meta in db.query(METAS_CUESTIONARIOS).filter(METAS_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario):
        print(meta.IDMeta)


    db.commit()

    return {
        "message": "Metas guardadas y estado actualizado correctamente",
        "idCuestionario": cuestionario.IDCuestionario
    }