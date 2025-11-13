from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from db import (
    SessionLocal,
    LISTAS_DE_COMPRAS,
    PLANIFICACIONES,
    PLANIFICACIONES_RECETAS,
    RECETAS_INGREDIENTES,
    INGREDIENTES,
    INGREDIENTES_LISTAS,
    LISTAS_PLANIFICACIONES,
    UNIDADES_MEDIDA,
    RECETAS
)
from datetime import datetime, date
from pydantic import BaseModel
from typing import Optional, List

router = APIRouter(prefix="/lista_compras", tags=["Lista de Compras"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

class IngredienteLista(BaseModel):
    idIngrediente: int
    id: int
    nombre: str
    nombreElemento: Optional[str]
    cantidad: Optional[float]
    unidad: Optional[str]
    disponibleItem: bool

class RecetaConIngredientes(BaseModel):
    id: int
    nombre: str
    ingredientes: List[IngredienteLista]

class ListaComprasResponse(BaseModel):
    idListaDeCompras: int
    fechaInicialLista: str
    fechaFinalLista: str
    recetas: List[RecetaConIngredientes]

def parse_ddmmyyyy(fecha_str: str) -> date:
    try:
        return datetime.strptime(fecha_str, "%d/%m/%Y").date()
    except ValueError:
        raise ValueError(f"Formato de fecha inválido: {fecha_str}. Debe ser dd/MM/yyyy")

@router.get("/obtener", response_model=ListaComprasResponse)
def generar_lista_compras_unica(
    idUser: int = Query(...),
    fechaInicio: str = Query(...),
    fechaFinal: str = Query(...),
    db: Session = Depends(get_db)
):
    try:
        fecha_inicio_date = parse_ddmmyyyy(fechaInicio)
        fecha_final_date = parse_ddmmyyyy(fechaFinal)
    except ValueError:
        raise HTTPException(status_code=400, detail="Formato de fecha inválido (usar dd/MM/yyyy)")

    planificaciones = db.query(PLANIFICACIONES).filter(
        PLANIFICACIONES.IDUser == idUser,
        PLANIFICACIONES.fechaPlanificacion >= fecha_inicio_date,
        PLANIFICACIONES.fechaPlanificacion <= fecha_final_date
    ).all()

    if not planificaciones:
        raise HTTPException(status_code=404, detail="No hay planificaciones en ese rango de fechas")

    # Crear lista combinada
    nueva_lista = LISTAS_DE_COMPRAS(
        IDUser=idUser,
        fechaInicialLista=fecha_inicio_date,
        fechaFinalLista=fecha_final_date,
        fechaCreacion=datetime.now()
    )
    db.add(nueva_lista)
    db.commit()
    db.refresh(nueva_lista)

    recetas_totales = []

    for plan in planificaciones:

        nueva_asociacion = LISTAS_PLANIFICACIONES(
        IDListaDeCompras=nueva_lista.IDListaDeCompras,
        IDPlanificacion=plan.IDPlanificacion
        )
        db.add(nueva_asociacion)

        recetas_plan = db.query(PLANIFICACIONES_RECETAS).filter(
            PLANIFICACIONES_RECETAS.IDPlanificacion == plan.IDPlanificacion
        ).all()

        for rp in recetas_plan:
            receta = db.query(RECETAS).filter(RECETAS.IDReceta == rp.IDReceta).first()
            if not receta:
                continue

            ingredientes_receta = db.query(RECETAS_INGREDIENTES).filter(
                RECETAS_INGREDIENTES.IDReceta == rp.IDReceta
            ).all()

            ingredientes_lista = []

            for ing_rec in ingredientes_receta:
                nuevo_item = INGREDIENTES_LISTAS(
                    IDListaDeCompras=nueva_lista.IDListaDeCompras,
                    IDIngrediente=ing_rec.IDIngrediente,
                    disponibleItem=False
                )
                db.add(nuevo_item)
                db.flush() 

                nombre_ingrediente = db.query(INGREDIENTES.nombreIngrediente).filter(
                    INGREDIENTES.IDIngrediente == ing_rec.IDIngrediente
                ).first()[0]

                unidad = None
                if ing_rec.IDUnidadMedida:
                    unidad = db.query(UNIDADES_MEDIDA.abreviacionUM).filter(
                        UNIDADES_MEDIDA.IDUnidadMedida == ing_rec.IDUnidadMedida
                    ).first()[0]

                ingredientes_lista.append(
                    IngredienteLista(
                        idIngrediente=ing_rec.IDIngrediente,
                        id=nuevo_item.ID,
                        nombre=nombre_ingrediente,
                        nombreElemento=None,
                        cantidad=ing_rec.cantidadIngrediente,
                        unidad=unidad,
                        disponibleItem=False
                    )
                )

            db.commit()

            recetas_totales.append(
                RecetaConIngredientes(
                    id=rp.IDReceta,
                    nombre=receta.nombreReceta,
                    ingredientes=ingredientes_lista
                )
            )

    return ListaComprasResponse(
        idListaDeCompras=nueva_lista.IDListaDeCompras,
        fechaInicialLista=str(fecha_inicio_date),
        fechaFinalLista=str(fecha_final_date),
        recetas=recetas_totales
    )

class ListaComprasResumen(BaseModel):
    idLista: int
    titulo: str
    fechaHora: str

@router.get("/usuario", response_model=List[ListaComprasResumen])
def obtener_listas_usuario(idUser: int = Query(...), db: Session = Depends(get_db)):
    listas = db.query(LISTAS_DE_COMPRAS).filter(LISTAS_DE_COMPRAS.IDUser == idUser).all()
    
    if not listas:
        raise HTTPException(status_code=404, detail="No hay listas de compras para este usuario")
    
    resumen_listas = []
    for lista in listas:
        titulo = f"Lista de compras {lista.IDListaDeCompras}"
        fecha_hora = lista.fechaCreacion.strftime("%d/%m/%Y - %H:%M")
        resumen_listas.append(
            ListaComprasResumen(
                idLista=lista.IDListaDeCompras,
                titulo=titulo,
                fechaHora=fecha_hora
            )
        )
    
    return resumen_listas

@router.get("/{idLista}", response_model=ListaComprasResponse)
def obtener_lista_por_id(idLista: int, db: Session = Depends(get_db)):
    lista = db.query(LISTAS_DE_COMPRAS).filter(LISTAS_DE_COMPRAS.IDListaDeCompras == idLista).first()
    if not lista:
        raise HTTPException(status_code=404, detail="Lista no encontrada")
    
    recetas_totales = []

    planificaciones = db.query(LISTAS_PLANIFICACIONES).filter(
        LISTAS_PLANIFICACIONES.IDListaDeCompras == idLista
    ).all()

    for lp in planificaciones:
        recetas_plan = db.query(PLANIFICACIONES_RECETAS).filter(
            PLANIFICACIONES_RECETAS.IDPlanificacion == lp.IDPlanificacion
        ).all()

        for rp in recetas_plan:
            receta = db.query(RECETAS).filter(RECETAS.IDReceta == rp.IDReceta).first()
            if not receta:
                continue

            ingredientes_receta = db.query(RECETAS_INGREDIENTES).filter(
                RECETAS_INGREDIENTES.IDReceta == rp.IDReceta
            ).all()

            ingredientes_lista = []
            for ing_rec in ingredientes_receta:
                rel = db.query(INGREDIENTES_LISTAS).filter(
                    INGREDIENTES_LISTAS.IDListaDeCompras == idLista,
                    INGREDIENTES_LISTAS.IDIngrediente == ing_rec.IDIngrediente
                ).first()
                disponible = rel.disponibleItem if rel else False

                nombre_ingrediente = db.query(INGREDIENTES.nombreIngrediente).filter(
                    INGREDIENTES.IDIngrediente == ing_rec.IDIngrediente
                ).first()[0]

                unidad = None
                if ing_rec.IDUnidadMedida:
                    unidad = db.query(UNIDADES_MEDIDA.abreviacionUM).filter(
                        UNIDADES_MEDIDA.IDUnidadMedida == ing_rec.IDUnidadMedida
                    ).first()[0]

                ingredientes_lista.append(
                    IngredienteLista(
                        idIngrediente=ing_rec.IDIngrediente,
                        id=rel.ID,
                        nombre=nombre_ingrediente,
                        nombreElemento=None,
                        cantidad=ing_rec.cantidadIngrediente,
                        unidad=unidad,
                        disponibleItem=disponible
                    )
                )

            recetas_totales.append(
                RecetaConIngredientes(
                    id=rp.IDReceta,
                    nombre=receta.nombreReceta,
                    ingredientes=ingredientes_lista
                )
            )

    return ListaComprasResponse(
        idListaDeCompras=lista.IDListaDeCompras,
        fechaInicialLista=str(lista.fechaInicialLista),
        fechaFinalLista=str(lista.fechaFinalLista),
        recetas=recetas_totales
    )


@router.put("/listas/ingrediente/{idIngredienteLista}/estado")
def actualizar_estado_ingrediente(
    idIngredienteLista: int,
    disponible: bool,
    db: Session = Depends(get_db)
):
    relacion = db.query(INGREDIENTES_LISTAS).filter(
        INGREDIENTES_LISTAS.ID == idIngredienteLista
    ).first()

    if not relacion:
        raise HTTPException(status_code=404, detail="Ingrediente no encontrado en la lista")

    relacion.disponibleItem = disponible
    db.commit()

    return {"mensaje": "Estado actualizado", "idIngredienteLista": idIngredienteLista, "disponible": disponible}