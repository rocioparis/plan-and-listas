import sys, os
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# ? META 1: LLEVAR UNA DIETA EQUILIBRADA
# * AUMENTAR: Vitaminas, minerales, proteínas, carbohidratos
# ! DISMINUIR: Colesterol

from importaciones import controlDifuso, np, libreriaLD
from nombresConjuntosOutput import nombresConjuntosOutput, obtener_conjunto_mayor
from sistemaDifuso import ejecutar_sistema_difuso
from conjuntoSalida import crear_conjuntos_salida
from noConsumibles import aplicarRestriccionesNutrientes

from data.modelos.nutrientes.vitaminas import (A, B1, B12, B2, B3, B6, Betacaroteno, C, K, acidoFolico, D, E)
from data.modelos.nutrientes.minerales import (Hierro, Magnesio, Calcio, Fosforo, Selenio, Sodio, Manganeso, Boro, Cobre, Zinc, Potasio, Yodo)
from data.modelos.nutrientes.macronutrientes import proteinas, Carbohidratos
from data.modelos.nutrientes.otros import Colesterol

# Listas de nutrientes
vitaminas = [C, E, acidoFolico, A, B1, B2, B3, B6, B12, Betacaroteno, K, D]
minerales = [Fosforo, Hierro, Magnesio, Calcio, Selenio, Manganeso, Boro, Cobre, Zinc, Potasio, Yodo, Sodio]
macronutrientes = [proteinas, Carbohidratos]
evitables = [Colesterol]

aumentar_nutrientes = vitaminas + minerales + macronutrientes
reducir_nutrientes = evitables

aumentar = controlDifuso.Antecedent(np.arange(0, 101, 1), 'aumentar')
reducir = controlDifuso.Antecedent(np.arange(0, 101, 1), 'reducir')

aumentar['mal'] = libreriaLD.trimf(aumentar.universe, [0,0,40])
aumentar['medio'] = libreriaLD.trimf(aumentar.universe, [30,60,80])
aumentar['bien'] = libreriaLD.trimf(aumentar.universe, [70,100,100])

reducir['bien'] = libreriaLD.trimf(reducir.universe, [0,0,40])
reducir['medio'] = libreriaLD.trimf(reducir.universe, [30,60,80])
reducir['mal'] = libreriaLD.trimf(reducir.universe, [70,100,100])

coherenciaNutricionalMeta1 = crear_conjuntos_salida('coherenciaNutricionalMeta1')
print("coherenciaNutricionalMeta1:", id(coherenciaNutricionalMeta1))

print(type(coherenciaNutricionalMeta1))
print(coherenciaNutricionalMeta1)

print(coherenciaNutricionalMeta1.label)

print(coherenciaNutricionalMeta1.terms)

reglas = [
    controlDifuso.Rule(aumentar['mal'] & reducir['mal'], coherenciaNutricionalMeta1['muyBaja']),
    controlDifuso.Rule(aumentar['mal'] & reducir['medio'], coherenciaNutricionalMeta1['baja']),
    controlDifuso.Rule(aumentar['medio'] & reducir['bien'], coherenciaNutricionalMeta1['media']),
    controlDifuso.Rule(aumentar['bien'] & reducir['medio'], coherenciaNutricionalMeta1['alta']),
    controlDifuso.Rule(aumentar['bien'] & reducir['bien'], coherenciaNutricionalMeta1['muyAlta'])
]

def evaluar_meta(inputs):
    # Trae los inputs de plan_comida.py (ej: 10 g Vitamina A, 20 g Calcio, etc.)

    # Define las categorías de nutrientes según este caso. (vitaminas, minerales, macronutrientes y evitables son las listas)
    categorias = {
        'vitaminas': vitaminas,
        'minerales': minerales,
        'macronutrientes': macronutrientes,
        'otros': evitables
    }

    # Si el usuario tiene la meta A y un nutriente B marcado como no consumible, y B se recomienda para cumplir A, se omite
    # el ver si se cumple con la recomendación de B para A
    # ? (ej. si el usuario marcó 'calcio' como no consumible en el cuestionario, entonces a los 20g de calcio se le asignan
    # ? 0 y solo se evalúan los 10g de vitamina A)
    aplicarRestriccionesNutrientes(inputs, categorias, getattr(inputs, 'nutrientes_no_consumibles', []))

    # Guardará los valores de los nutrientes que quiere aumentar (nutr recomendados para esta meta)
    valores_aumentar = []
    for categoria in ['vitaminas', 'minerales', 'macronutrientes']:
        # En grupo obtiene inputs, categoria y None si no existe 
        # ? (ejemplo: 10g Vitamina A, 'vitaminas' - 20g Calcio, 'minerales')
        grupo = getattr(inputs, categoria, None)
        if grupo:
            # Recorre los nutrientes definidos para esa categoría 
            # ? (ej: recorre nutrientes en 'vitaminas' y 'minerales')
            for v in categorias[categoria]:
                # Obtiene el nombre del nutriente a chequear 
                # ? (ej: obtiene, en 'vitaminas' Vitamina A, y en 'minerales' Calcio)
                nombre_attr = getattr(v, 'label', None) or v.__name__
                # Verifica si el grupo tiene ese nutriente como atributo 
                # ? (ej. se fija si está Vitamina A y Calcio -> están)
                if hasattr(grupo, nombre_attr):
                    # De ser así, agrega el valor del nutriente a la lista valores_aumentar 
                    # ? (ej. 10g y 20g a valores_aumentar)
                    valores_aumentar.append(getattr(grupo, nombre_attr))
    valores_validos = [v for v in valores_aumentar if v > 0]
    promedio_aumentar = np.mean(valores_validos) if valores_validos else 0

    # Guardará los valores de los nutrientes que quiere reducir (nutr NO recomendados para esta meta)
    valores_reducir = []
    # Como es una sola categoría, no hacemos un for. 
    # ? ej: puede obtener 30g Colesterol, 'otros'
    grupo_otros = getattr(inputs, 'otros', None)
    # ? A este if no entrará, porque como este ejemplo no tiene colesterol, entonces "grupo_otros" no guarda nada
    if grupo_otros:
        # Pero si la receta tuviese colesterol:
        # ! Recorre los nutrientes definidos para la categoría 'otros' (Colesterol)
        for v in categorias['otros']:
            # ! Obtiene Colesterol en 'otros'
            nombre_attr = getattr(v, 'label', None) or v.__name__
            # ! Verifica si en el grupo está 'Colesterol'
            if hasattr(grupo_otros, nombre_attr):
                # ! De ser así, agrega el valor de 30g a valores_reducir
                valores_reducir.append(getattr(grupo_otros, nombre_attr))
    # ! promedio_reducir tendrá el promedio de 30g
    valores_validos_reducir = [v for v in valores_reducir if v > 0]
    promedio_reducir = np.mean(valores_validos_reducir) if valores_validos_reducir else 0

    # inputs_sistema guarda dos diccionarios:
    # ? Ej inputs_sistema = { 'aumentar': promedio de 10g y 20g, 'reducir': promedio de 0g }
    # ! Ej inputs_sistema = { 'aumentar': promedio de 0g, 'reducir: promedio de 30g }
    inputs_sistema = {'aumentar': promedio_aumentar, 'reducir': promedio_reducir}

    print("NombreSalida:", 'coherenciaNutricionalMeta1')
    print("Label del Consequent:", coherenciaNutricionalMeta1.label)

    # Al sistema difuso le pasa las reglas, el string del consecuente (para log), el consecuente itself, y el inputs_sistema('aumentar': promedio x, 'reducir': promedio x)
    # ? Ej: 40 
    # ?     'muyBaja' = 0, 'baja' = 2, 'media' = 90, 'alta' = 3, 'muyAlta' = 0 
    salida, grados = ejecutar_sistema_difuso(reglas, 'coherenciaNutricionalMeta1', coherenciaNutricionalMeta1, inputs_sistema)

    # ? conjuntoConMayorPertenencia = 'media'
    conjuntoConMayorPertenencia = obtener_conjunto_mayor(grados)

    # ? devuelve:
    # ? 'porcentaje': 40,
    # ? 'nivel': MEDIA
    return {
        'porcentaje': salida,
        'nivel': nombresConjuntosOutput[conjuntoConMayorPertenencia]
    }