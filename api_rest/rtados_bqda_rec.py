from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from db import RECETAS, CUESTIONARIOS, NUTRIENTES_CUESTIONARIOS
from api_rest.api_rest import get_db
from recomendaciones import obtener_resultados_bqda as logica_recetas_bqda
from typing import Optional
from fastapi import Query

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

@router.get("/buscar_recetas/")
def obtener_recetas_recomendadas_endpoint_bqda(
    id_user: Optional[int] = Query(None, description="ID del usuario, opcional si no está logueado"),
    query: str = Query("", description="Palabras clave para buscar recetas"),
    db: Session = Depends(get_db)
):
    # buscar el cuestionario del usuario solo si hay id_user
    nutrientes_no_consumibles = []
    if id_user is not None:
        cuestionario = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == id_user).first()
        if cuestionario:
            nutrientes_no_consumibles = [
                n.IDNutriente
                for n in db.query(NUTRIENTES_CUESTIONARIOS)
                        .filter(NUTRIENTES_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario)
                        .all()
            ]

    recetas = logica_recetas_bqda(nutrientes_no_consumibles, db)

    if query:
        query_lower = query.lower()
        recetas = [r for r in recetas if query_lower in r["nombre"].lower()]

    return recetas