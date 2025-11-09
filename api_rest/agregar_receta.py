from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from db import SessionLocal, UNIDADES_MEDIDA
from typing import List
from pydantic import BaseModel

router = APIRouter(prefix="/unidades", tags=["Unidades de medida"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

class UnidadResponse(BaseModel):
    abreviacion: str

@router.get("/", response_model=List[UnidadResponse])
def obtener_unidades(db: Session = Depends(get_db)):
    unidades = db.query(UNIDADES_MEDIDA.abreviacionUM).all()
    return [{"abreviacion": u[0]} for u in unidades]