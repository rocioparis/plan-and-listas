from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from db import RECETAS, RECETAS_INGREDIENTES, INGREDIENTES, UNIDADES_MEDIDA, PLANIFICACIONES, PLANIFICACIONES_RECETAS
from api_rest import get_db
from pydantic import BaseModel
from typing import List, Optional

router = APIRouter()

# Modelos Pydantic para la respuesta
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

@router.get("/receta_por_plan/{id_plan}", response_model=RecetaModel)
def obtener_receta(id_plan: int, db: Session = Depends(get_db)):
    plan = db.query(PLANIFICACIONES).filter(PLANIFICACIONES.IDPlanificacion == id_plan).first()
    if not plan:
        raise HTTPException(status_code=404, detail="Planificación no encontrada")
    
    relac_rec_plan = db.query(PLANIFICACIONES_RECETAS).filter(
        PLANIFICACIONES_RECETAS.IDPlanificacion == id_plan
    ).all()

    receta_detalle = []
    for relac in relac_rec_plan:
        receta = db.query(RECETAS).filter(
            RECETAS.IDReceta == relac.IDReceta
        ).first()

        relaciones = db.query(RECETAS_INGREDIENTES).filter(
            RECETAS_INGREDIENTES.IDReceta == receta.IDReceta
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