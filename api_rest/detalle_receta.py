from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from db import RECETAS, RECETAS_INGREDIENTES, INGREDIENTES, UNIDADES_MEDIDA
from api_rest.api_rest import get_db
from pydantic import BaseModel
from typing import List, Optional

router = APIRouter()

class IngredienteModel(BaseModel):
    id: int
    nombre: Optional[str]
    cantidad: Optional[float]
    unidad: Optional[str]

class RecetaModel(BaseModel):
    id: int
    nombre: str
    imagen_url: str
    ingredientes: List[IngredienteModel]
    procedimiento: str

@router.get("/detalle_receta/{id_receta}", response_model=RecetaModel)
def obtener_receta(id_receta: int, db: Session = Depends(get_db)):
    receta = db.query(RECETAS).filter(RECETAS.IDReceta == id_receta).first()
    if not receta:
        raise HTTPException(status_code=404, detail="Receta no encontrada")

    relaciones = db.query(RECETAS_INGREDIENTES).filter(
        RECETAS_INGREDIENTES.IDReceta == id_receta
    ).all()

    ingredientes_detalle = []
    for rel in relaciones:
        ingrediente = db.query(INGREDIENTES).filter(
            INGREDIENTES.IDIngrediente == rel.IDIngrediente
        ).first()
        unidad = db.query(UNIDADES_MEDIDA).filter(
            UNIDADES_MEDIDA.IDUnidadMedida == rel.IDUnidadMedida
        ).first()
        ingredientes_detalle.append(
            IngredienteModel(
                id=ingrediente.IDIngrediente,
                nombre=ingrediente.nombreIngrediente if ingrediente else None,
                cantidad=rel.cantidadIngrediente,
                unidad=unidad.abreviacionUM if unidad else None
            )
        )

    return RecetaModel(
        id=receta.IDReceta,
        nombre=receta.nombreReceta,
        imagen_url=receta.imagenReceta,
        ingredientes=ingredientes_detalle,
        procedimiento=receta.procedimiento
    )