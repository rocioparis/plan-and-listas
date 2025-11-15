import random
from db import (
    RECETAS,
    NUTRIENTES_RECETAS,
    NUTRIENTES,
    CATEGORIAS_NUTRIENTES,
    UNIDADES_MEDIDA,
    SessionLocal
)
from sqlalchemy.orm import Session
from importaciones import BaseModel
from typing import Optional, List

# Función para convertir unidades -> Objetivo:
# ? Toma valor real en unidad original, convierte todo a gramos para que las unidades sean comparables, calcula qué % 
# ? de recomendación diaria representa el nutriente. Prepara los datos en porcentaje, porque después cada meta usará 
# ? dichos porcentajes para evaluar la coherencia nutricional
# Unidades en NUTRIENTES: g, mg, mcg
def convertir(valor, unidad):
   # unidad = unidad.lower()
    if unidad == "mg":
        return valor / 1000      # mg → g
    elif unidad == "mcg":
        return valor / 1_000_000  # mcg → g
    else:
        return valor              # ya está en g


# Normalizar nutrientes a porcentaje de recomendación diaria
def normalizar_nutrientes(inputs):
    valores_recomendados = {
        "proteinas": (75, "g"),
        "Carbohidratos": (275, "g"),
        "grasasTotales": (66.7, "g"),
        "grasasSaturadas": (5.55, "g"),
        "Azucar": (5.55, "g"),
        "Fibra": (25, "g"),
        "Calcio": (1, "g"),
        "Hierro": (0.018, "g"),
        "Magnesio": (0.0018, "g"),
        "Zinc": (0.008, "g"),
        "Selenio": (0.000055, "g"),
        "Fosforo": (0.0007, "g"),
        "Sodio": (1.5, "g"),
        "Potasio": (4.7, "g"),
        "Yodo": (0.00015, "g"),
        "Manganeso": (0.0018, "g"),
        "Boro": (0.0005, "g"),
        "Cobre": (0.0009, "g"),
        "Colesterol": (0.15, "g"),
        "Cafeina": (0.2, "g"),
        "A": (0.0007, "g"),
        "Betacaroteno": (0.0007, "g"),
        "B1": (0.0011, "g"),
        "B2": (0.0011, "g"),
        "B3": (0.014, "g"),
        "B6": (0.0013, "g"),
        "B12": (0.0000024, "g"),
        "C": (0.075, "g"),
        "D": (0.000015, "g"),
        "E": (0.015, "g"),
        "K": (0.00005796, "g"),
        "acidoFolico": (0.0004, "g"),
    }

    for grupo in [inputs.vitaminas, inputs.minerales, inputs.macronutrientes, inputs.otros]:
        for campo, valor in grupo.__dict__.items():
            if campo in valores_recomendados:
                recomendado, unidad_ref = valores_recomendados[campo]
              #  porcentaje = (convertir(valor, "g") / recomendado) * 100 if recomendado else 0
              #  porcentaje = (convertir(valor, unidad_desc) / recomendado) * 100 if recomendado else 0
                porcentaje = (valor / recomendado) * 100 if recomendado else 0
                setattr(grupo, campo, porcentaje)
    return inputs

class Vitaminas(BaseModel):
    A: float
    Betacaroteno: float
    B1: float
    B2: float
    B3: float
    B6: float
    B12: float
    C: float
    D: float
    E: float
    K: float
    acidoFolico: float

class Minerales(BaseModel):
    Hierro: float
    Calcio: float
    Magnesio: float
    Zinc: float
    Selenio: float
    Fosforo: float
    Yodo: float
    Sodio: float
    Potasio: float
    Manganeso: float
    Boro: float
    Cobre: float

class Macronutrientes(BaseModel):
    proteinas:float
    grasasTotales: float
    grasasSaturadas: float
    Carbohidratos: float
    Azucar: float
    Fibra: float

class Otros(BaseModel):
    Colesterol: float
    Cafeina: float

class Inputs(BaseModel):
    vitaminas: Vitaminas
    minerales: Minerales
    macronutrientes: Macronutrientes
    otros: Otros
    nutrientes_no_consumibles: Optional[List[str]] = []

# Crear inputs desde una receta almacenada
def crear_inputs_desde_receta(receta, nutrientes_no_consumibles):
    db = SessionLocal()

    # Inicializar todos los valores a 0
    vitaminas = Vitaminas(
        A=0, Betacaroteno=0, B1=0, B2=0,
        B3=0, B6=0, B12=0, C=0,
        D=0, E=0, K=0, acidoFolico=0
    )
    minerales = Minerales(
        Hierro=0, Calcio=0, Magnesio=0, Zinc=0, Selenio=0,
        Fosforo=0, Yodo=0, Sodio=0, Potasio=0, Manganeso=0, Boro=0, Cobre=0
    )
    macronutrientes = Macronutrientes(
        proteinas=0, grasasTotales=0, grasasSaturadas=0,
        Carbohidratos=0, Azucar=0, Fibra=0
    )
    otros = Otros(Colesterol=0, Cafeina=0)

    nombre_map_vitaminas = {
        "a": "A",
        "betacaroteno": "Betacaroteno",
        "b1": "B1",
        "b2": "B2",
        "b3": "B3",
        "b6": "B6",
        "b12": "B12",
        "c": "C",
        "d": "D",
        "e": "E",
        "k": "K",
        "acidofolico": "acidoFolico"
    }

    nombre_map_minerales = {
        "hierro": "Hierro",
        "calcio": "Calcio",
        "magnesio": "Magnesio",
        "zinc": "Zinc",
        "selenio": "Selenio",
        "fosforo": "Fosforo",
        "yodo": "Yodo",
        "sodio": "Sodio",
        "potasio": "Potasio",
        "manganeso": "Manganeso",
        "boro": "Boro",
        "cobre": "Cobre"
    }

    nombre_map_macronutrientes = {
        "proteinas": "proteinas",
        "grasastotales": "grasasTotales",
        "grasassaturadas": "grasasSaturadas",
        "carbohidratos": "Carbohidratos",
        "azucar": "Azucar",
        "fibra": "Fibra"
    }

    nombre_map_otros = {
        "colesterol": "Colesterol",
        "cafeina": "Cafeina"
    }

    # En NUTRIENTES_RECETA filtra el IDReceta corresp.
    rn = db.query(NUTRIENTES_RECETAS).filter(NUTRIENTES_RECETAS.IDReceta == receta["id"]).all()

    # Para cada IDNutriente asociado al IDReceta correspondiente
    for r in rn:
        nutr = db.query(NUTRIENTES).filter(NUTRIENTES.IDNutriente == r.IDNutriente).first()
        if not nutr:
            continue

            # Busca la categoría
        cat = db.query(CATEGORIAS_NUTRIENTES).filter(CATEGORIAS_NUTRIENTES.IDCategoria == nutr.IDCategoria).first()
        if not cat:
            continue

            # La unidad de medida
        unidad = db.query(UNIDADES_MEDIDA).filter(UNIDADES_MEDIDA.IDUnidadMedida == nutr.IDUnidadMedida).first()
            # Obtiene la abreviación (g, mcg, etc). Si no tiene, lo toma como 'g' (gramos) -> Todas tienen, pero para asegurarse
        unidad_desc = unidad.abreviacionUM.lower() if unidad else "g"

        # Convierte el valor del nutriente en la receta a la unidad base (gramos)
        valor = convertir(r.valor, unidad_desc)
        nombre = nutr.nombreNutriente.lower().replace(" ", "")

        # Ejemplo primer if: Verifica que el nutriente en la receta sea una vitamina Y revisa que el nombre del nutriente esté en el diccionario nombre_map_vitaminas para mapearlo al atributo correcto de la clase Vitaminas
        if cat.nombreCategoria.lower() == "vitaminas" and nombre in nombre_map_vitaminas:
            # Incluye el valor (convertido a gramos) del nutriente en el atributo del objeto (ej. valor = 0.05 Y nutriente = vitamina A -> vitaminas.A = 0.05)
            setattr(vitaminas, nombre_map_vitaminas[nombre], valor)
        elif cat.nombreCategoria.lower() == "minerales" and nombre in nombre_map_minerales:
            setattr(minerales, nombre_map_minerales[nombre], valor)
        elif cat.nombreCategoria.lower() == "macronutrientes" and nombre in nombre_map_macronutrientes:
            setattr(macronutrientes, nombre_map_macronutrientes[nombre], valor)
        elif cat.nombreCategoria.lower() == "otros" and nombre in nombre_map_otros:
            setattr(otros, nombre_map_otros[nombre], valor)

    db.close()

    
    # DEBUG: imprimir todos los valores que se van a pasar al fuzzy
    print("DEBUG: Valores de vitaminas:")
    for k, v in vars(vitaminas).items():
        print(f"  {k}: {v}")

    print("DEBUG: Valores de minerales:")
    for k, v in vars(minerales).items():
        print(f"  {k}: {v}")

    print("DEBUG: Valores de macronutrientes:")
    for k, v in vars(macronutrientes).items():
        print(f"  {k}: {v}")

    print("DEBUG: Valores de otros:")
    for k, v in vars(otros).items():
        print(f"  {k}: {v}")

    # Acá devuelve los inputs para la receta -> ejemplo: 10 g Vitamina A, 20 g Calcio, etc.
    inputs = Inputs(
        vitaminas=vitaminas,
        minerales=minerales,
        macronutrientes=macronutrientes,
        otros=otros,
        nutrientes_no_consumibles = nutrientes_no_consumibles
    )

    inputs = normalizar_nutrientes(inputs)

    return inputs


# Obtener todas las recetas
def obtener_todas_las_recetas(db: Session = None):
    if db:
        return [
            {
                "id": r.IDReceta,
                "nombre": r.nombreReceta,
                "imagen_url": r.imagenReceta
            }
            for r in db.query(RECETAS).filter(RECETAS.IDUser == None).all()
        ]


# Buscará si los nutrientes marcados como no consumibles están en la tabla NUTRIENTES_RECETAS
def contiene_restringidos(receta, restricciones_ids, db: Session):
    nutrientes_receta = [
        r.IDNutriente
        for r in db.query(NUTRIENTES_RECETAS)
        .filter(NUTRIENTES_RECETAS.IDReceta == receta["id"])
        .all()
    ]
    # Por lo tanto, no devolverá aquellas recetas que tengan los nutrientes que el usuario no pueda consumir
    return any(n in nutrientes_receta for n in restricciones_ids)

# Para mostrar en el panel de la pantalla de inicio
# ! Primero se prueba con nutrientes
def obtener_recetas_recomendadas(nutrientes_no_consumibles, db: Session = None):
    recetas = obtener_todas_las_recetas(db)

    # Si el usuario no marcó ningún nutriente como no consumible
    if not nutrientes_no_consumibles:
        return recetas

    recetas_filtradas = []
    temp_db = Session(bind=db.bind)
    try:
        for r in recetas:
            if nutrientes_no_consumibles and contiene_restringidos(r, nutrientes_no_consumibles, temp_db):
                continue
            recetas_filtradas.append(r)
    finally:
        temp_db.close()

    if not recetas_filtradas:
        return recetas

    # Enviará lo que se tiene que mostrar en el panel (imagen y nombre de cada receta)
    recetas_para_front = [
        {"id": r["id"],"nombre": r["nombre"], "imagen_url": r["imagen_url"]}
        for r in recetas_filtradas
    ]

    print("Recetas que se envían al front:", recetas_para_front)
    return recetas_para_front

def obtener_resultados_bqda(nutrientes_no_consumibles, db: Session = None):
    recetas = obtener_todas_las_recetas(db)

    if not nutrientes_no_consumibles:
        return recetas

    recetas_filtradas = []
    temp_db = Session(bind=db.bind)
    try:
        for r in recetas:
            if nutrientes_no_consumibles and contiene_restringidos(r, nutrientes_no_consumibles, temp_db):
                continue
            recetas_filtradas.append(r)
    finally:
        temp_db.close()

    if not recetas_filtradas:
        return recetas

    recetas_para_front = [
        {"nombre": r["nombre"], "imagen_url": r["imagen_url"]}
        for r in recetas_filtradas
    ]

    print("Recetas que se envían al front:", recetas_para_front)
    return recetas_para_front