from fastapi import APIRouter, Depends
import importlib
import sys
import os
from sqlalchemy.orm import Session
from db import (
    SessionLocal,
    PLANIFICACIONES,
    PLANIFICACIONES_RECETAS,
    RECETAS,
    CUESTIONARIOS,
    METAS_CUESTIONARIOS,
    METAS,
    NUTRIENTES_CUESTIONARIOS,
    NUTRIENTES,
    COMIDAS_DIARIAS
)
from datetime import datetime
from api_rest.recomendaciones import crear_inputs_desde_receta

router = APIRouter(prefix="/plan_comida", tags=["Plan de Comida"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'data', 'modelos', 'metas')))

@router.post("/agregar")
def agregar_receta_al_plan(idReceta: int, idUser: int, fecha: str, db: Session = Depends(get_db)):
    fecha_dt = datetime.fromisoformat(fecha).date()

    # En la base, buscar la receta según el IDReceta recibido
    receta = db.query(RECETAS).filter(RECETAS.IDReceta == idReceta).first()
    if not receta:
        return {"mensaje": "Receta no encontrada"}

    # Obtener metas del usuario
    cuestionarios = db.query(CUESTIONARIOS).filter(CUESTIONARIOS.IDUser == idUser).all()
    metas_usuario = {}

    nutrientes_no_consumibles = []

    for cuestionario in cuestionarios:
        metas_cuestionario = db.query(METAS_CUESTIONARIOS).filter(
            METAS_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario
        ).all()

        nutrientes_cuestionario = db.query(NUTRIENTES_CUESTIONARIOS).filter(
            NUTRIENTES_CUESTIONARIOS.IDCuestionario == cuestionario.IDCuestionario
        ).all()

        # Cada IDNutriente conectado al IDCuestionario
        for nutriente_c in nutrientes_cuestionario:
            nutriente_db = db.query(NUTRIENTES).filter(NUTRIENTES.IDNutriente == nutriente_c.IDNutriente).first()
            if nutriente_db and nutriente_db.nombreNutriente:
                nutrientes_no_consumibles.append(nutriente_db.nombreNutriente)

        # Tomar cada IDMeta conectada al IDCuestionario dado
        for meta_c in metas_cuestionario:
            meta_id = meta_c.IDMeta
            if meta_id in metas_usuario:
                continue

            # El nombre
            meta_db = db.query(METAS).filter(METAS.IDMeta == meta_id).first()
            nombre_meta = meta_db.nombreMeta if meta_db else f"Meta {meta_id}"

            # Importar función de evaluación de la meta correspondiente desde archivo metaX.py
            nombre_archivo = f"meta{meta_id}"
            try:
                modulo = importlib.import_module(nombre_archivo)
                metas_usuario[meta_id] = {
                    "nombre": nombre_meta,
                    "evaluar": modulo.evaluar_meta
                }
            except ModuleNotFoundError:
                print(f"⚠️ No se encontró el archivo meta{meta_id}.py")

    # Si user no tiene metas, no evalúa. Acá iría a la pantalla "Datos de la planificación"
    if not metas_usuario | metas_usuario==11:
        coherencia = None

    # Crear inputs desde la receta
    receta_dict = {"id": receta.IDReceta}
    inputs = crear_inputs_desde_receta(receta_dict, nutrientes_no_consumibles=nutrientes_no_consumibles)
    # Inputs traería, por ejemplo, 10 g Vitamina A, 20 g Calcio, etc.

    # Evaluar coherencia de cada meta
    coherencia = {}
    # Para cada meta del usuario, en "resultado" guarda 
    for id_meta, data in metas_usuario.items():
        # ? resultado = {40, MEDIA}
        resultado = data["evaluar"](inputs)
        nivel = resultado["nivel"]

        botones = ["CANCELAR", "PLANIFICAR"]

        # ? coherencia[1] = { "nombre": Llevar una dieta equilibrada, "porcentaje": 40.00%, "nivel": MEDIA, botones: ["CANCELAR", "ALTERNATIVAS", "PLANIFICAR"]}
        coherencia[id_meta] = {
            "nombre": data["nombre"],
            "porcentaje": f'{resultado["porcentaje"]:.2f}%',
            "nivel": nivel,
            "botones": botones
        }
    coherencia = coherencia or {}
    return {
        "mensaje": "Coherencia calculada",
        "idReceta": idReceta,
        "coherencia": coherencia
    }

@router.post("/confirmar")
def confirmar_planificacion(
    idReceta: int,
    idUser: int,
    fecha: str,
    comensales: int,
    tipoComida: str,
    db: Session = Depends(get_db)
):
    fecha_dt = datetime.fromisoformat(fecha).date()

    comida = db.query(COMIDAS_DIARIAS).filter(
        COMIDAS_DIARIAS.nombreComida == tipoComida
    ).first()

    if not comida:
        return {"mensaje": f"Tipo de comida '{tipoComida}' no encontrado"}

    id_comida = comida.IDComida

    plan = db.query(PLANIFICACIONES).filter(
        PLANIFICACIONES.IDUser == idUser,
        PLANIFICACIONES.fechaPlanificacion == fecha_dt,
        PLANIFICACIONES.IDComida == id_comida
    ).first()

    if not plan:
        plan = PLANIFICACIONES(
            IDUser=idUser,
            IDComida=id_comida,
            fechaPlanificacion=fecha_dt,
            numeroComensales=comensales
        )
        db.add(plan)
        db.commit()
        db.refresh(plan)
    else:
        # Si ya existía, actualiz. el número de comensales
        plan.numeroComensales = comensales
        db.commit()

    plan_receta = PLANIFICACIONES_RECETAS(
        IDPlanificacion=plan.IDPlanificacion,
        IDReceta=idReceta
    )
    db.add(plan_receta)
    db.commit()

    return {
        "mensaje": "Comida planificada con éxito",
        "idPlanificacion": plan.IDPlanificacion,
        "idReceta": idReceta,
        "comensales": plan.numeroComensales
    }

@router.get("/por_fecha")
def obtener_comidas_planificadas(idUser: int, fecha: str, db: Session = Depends(get_db)):
    fecha_dt = datetime.fromisoformat(fecha).date()

    planificaciones = db.query(PLANIFICACIONES).filter(
        PLANIFICACIONES.IDUser == idUser,
        PLANIFICACIONES.fechaPlanificacion == fecha_dt
    ).all()

    comidas = []
    for plan in planificaciones:
        recetas = db.query(PLANIFICACIONES_RECETAS).filter(
            PLANIFICACIONES_RECETAS.IDPlanificacion == plan.IDPlanificacion
        ).all()

        for pr in recetas:
            receta_db = db.query(RECETAS).filter(RECETAS.IDReceta == pr.IDReceta).first()
            comida_tipo = db.query(COMIDAS_DIARIAS).filter(COMIDAS_DIARIAS.IDComida == plan.IDComida).first()
            if receta_db and comida_tipo:
                comidas.append({
                    "id": pr.IDReceta,
                    "nombre": receta_db.nombreReceta,
                    "tipoComida": comida_tipo.nombreComida,
                    "imagen_url": receta_db.imagenReceta
                })
    return comidas