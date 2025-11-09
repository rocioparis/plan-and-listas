from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from db import SessionLocal, RECETAS, RECETAS_INGREDIENTES, INGREDIENTES, UNIDADES_MEDIDA

router = APIRouter(prefix="/recetas", tags=["Recetas"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

class IngredienteIn(BaseModel):
    nombre: str
    cantidad: float
    unidad: str

class AgregarRecetaRequest(BaseModel):
    nombre: str
    procedimiento: str
    ingredientes: List[IngredienteIn]
    imagenUri: Optional[str] = None
    IDUser: int

class RecetaOut(BaseModel):
    id: int
    nombre: str
    procedimiento: str
    imagenUri: Optional[str] = None
    IDUser: int

@router.post("/agregar", response_model=RecetaOut, status_code=status.HTTP_201_CREATED)
def agregar_receta(request: AgregarRecetaRequest, db: Session = Depends(get_db)):
    nueva = RECETAS(
        nombreReceta=request.nombre,
        procedimiento=request.procedimiento,
        imagenReceta=request.imagenUri,
        IDUser=request.IDUser
    )
    db.add(nueva)
    db.commit()
    db.refresh(nueva)

    for ing in request.ingredientes:
        ingrediente_obj = db.query(INGREDIENTES).filter(INGREDIENTES.nombreIngrediente == ing.nombre).first()
        if not ingrediente_obj:
            ingrediente_obj = INGREDIENTES(nombreIngrediente=ing.nombre)
            db.add(ingrediente_obj)
            db.commit()
            db.refresh(ingrediente_obj)

        unidad_obj = None
        if ing.unidad:
            unidad_obj = db.query(UNIDADES_MEDIDA).filter(UNIDADES_MEDIDA.abreviacionUM == ing.unidad).first()
            if not unidad_obj:
                raise HTTPException(status_code=400, detail=f"Unidad de medida inexistente: {ing.unidad}")

        rec_ing = RECETAS_INGREDIENTES(
            IDReceta=nueva.IDReceta,
            IDIngrediente=ingrediente_obj.IDIngrediente,
            cantidadIngrediente=ing.cantidad,
            IDUnidadMedida=unidad_obj.IDUnidadMedida if unidad_obj else None
        )
        db.add(rec_ing)

    db.commit()

    return RecetaOut(
        id=nueva.IDReceta,
        nombre=nueva.nombreReceta,
        procedimiento=nueva.procedimiento,
        imagenUri=nueva.imagenReceta,
        IDUser=request.IDUser
    )

from typing import List
@router.get("/recetas_por_user/{IDUser}", response_model=List[RecetaOut])
def obtener_recetas_user(IDUser: int, db: Session = Depends(get_db)):
    recetas = db.query(RECETAS).filter(RECETAS.IDUser == IDUser).all()
    
    return [
        RecetaOut(
            id=r.IDReceta,
            nombre=r.nombreReceta,
            procedimiento=r.procedimiento,
            imagenUri=r.imagenReceta,
            IDUser=r.IDUser
        )
        for r in recetas
    ]