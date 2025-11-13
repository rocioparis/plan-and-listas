from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from db import RECETAS, CUESTIONARIOS, NUTRIENTES_CUESTIONARIOS
from api_rest.api_rest import get_db
from api_rest.recomendaciones import obtener_recetas_recomendadas

router = APIRouter()

@router.get("/recetas/")
def obtener_recetas(db: Session = Depends(get_db)):
    recetas = db.query(RECETAS).filter(RECETAS.IDUser == None).all()
    return [
        {
            "nombre": r.nombreReceta,
            "imagen_url": r.imagenReceta
        }
        for r in recetas
    ]

@router.get("/recetas/recomendadas/{id_user}")
def obtener_recetas_recomendadas_endpoint(id_user: int, db: Session = Depends(get_db)):
    cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == id_user).first()

    nutrientes_no_consumibles = []

    # Es decir, no le mostrará aquellas recetas con nutrientes marcados por el usuario como no consumibles
    if cuestionario:
        nutrientes_no_consumibles = [
            n.IDNutriente
            for n in db.query(NUTRIENTES_CUESTIONARIOS)
                    .filter(NUTRIENTES_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario)
                    .all()
        ]

    recetas = obtener_recetas_recomendadas(nutrientes_no_consumibles, db)

    return recetas