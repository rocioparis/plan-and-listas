import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# ? PREVENIR ENFERMEDADES CARDIOVASCULARES:
# * AUMENTAR: vitamina A, vitamina C, 
# * vitamina E, vitamina B1, vitamina B2, 
# * vitamina B3, vitamina B6, ácido fólico,
# * cobre, magnesio, hierro, fósforo, zinc, 
# * manganeso, fibra, proteínas, carbohidratos, 
# * potasio, yodo, calcio, selenio
# ! REDUCIR: colesterol, azúcar, grasas saturadas, exceso de sodio

from importaciones import controlDifuso, np, libreriaLD
from nombresConjuntosOutput import nombresConjuntosOutput, obtener_conjunto_mayor
from sistemaDifuso import ejecutar_sistema_difuso
from conjuntoSalida import crear_conjuntos_salida
from noConsumibles import aplicarRestriccionesNutrientes

from data.modelos.nutrientes.vitaminas import (A, C, E, B1, B2, B3, B6, acidoFolico)
from data.modelos.nutrientes.minerales import (Cobre, Magnesio, Hierro, Fosforo, Zinc, Manganeso, Potasio, Yodo, Calcio,
Selenio, Sodio)
from data.modelos.nutrientes.macronutrientes import proteinas, Carbohidratos, Azucar, grasasSaturadas, Fibra
from data.modelos.nutrientes.otros import Colesterol

vitaminas = [C, A, B1, B2, B3, E, B6, acidoFolico]
minerales = [Fosforo, Hierro, Magnesio, Calcio, Selenio, Zinc, Yodo, Potasio, Cobre, Manganeso, Sodio]
macronutrientes = [proteinas, Fibra, Carbohidratos]
evitables = [Colesterol, Azucar, grasasSaturadas]

aumentar_nutrientes = vitaminas + minerales + macronutrientes
reducir_nutriente = evitables

aumentar = controlDifuso.Antecedent(np.arange(0, 101, 1), 'aumentar')
reducir = controlDifuso.Antecedent(np.arange(0, 101, 1), 'reducir')

aumentar['mal'] = libreriaLD.trimf(aumentar.universe, [0,0,40])
aumentar['medio'] = libreriaLD.trimf(aumentar.universe, [30,60,80])
aumentar['bien'] = libreriaLD.trimf(aumentar.universe, [70,100,100])

reducir['bien'] = libreriaLD.trimf(reducir.universe, [0,0,40])
reducir['medio'] = libreriaLD.trimf(reducir.universe, [30,60,80])
reducir['mal'] = libreriaLD.trimf(reducir.universe, [70,100,100])

coherenciaNutricionalMeta5 = crear_conjuntos_salida('coherenciaNutricionalMeta5')
print("coherenciaNutricionalMeta5:", id(coherenciaNutricionalMeta5))

reglas = [
    controlDifuso.Rule(aumentar['mal'] & reducir['mal'], coherenciaNutricionalMeta5['muyBaja']),
    controlDifuso.Rule(aumentar['mal'] & reducir['medio'], coherenciaNutricionalMeta5['baja']),
    controlDifuso.Rule(aumentar['medio'] & reducir['bien'], coherenciaNutricionalMeta5['media']),
    controlDifuso.Rule(aumentar['bien'] & reducir['medio'], coherenciaNutricionalMeta5['alta']),
    controlDifuso.Rule(aumentar['bien'] & reducir['bien'], coherenciaNutricionalMeta5['muyAlta'])
]

def evaluar_meta(inputs):
    categorias = {
        'vitaminas': vitaminas,
        'minerales': minerales,
        'macronutrientes': macronutrientes,
        'otros': evitables
    }

    aplicarRestriccionesNutrientes(inputs, categorias, getattr(inputs, 'nutrientes_no_consumibles', []))

    valores_aumentar = []
    for categoria in ['vitaminas', 'minerales', 'macronutrientes']:
        grupo = getattr(inputs, categoria, None)
        if grupo:
            for v in categorias[categoria]:
                nombre_attr = getattr(v, 'label', None) or v.__name__
                if hasattr(grupo, nombre_attr):
                    valores_aumentar.append(getattr(grupo, nombre_attr))
    valores_validos = [v for v in valores_aumentar if v > 0]
    promedio_aumentar = np.mean(valores_validos) if valores_validos else 0

    valores_reducir = []
    grupo_otros = getattr(inputs, 'otros', None)
    if grupo_otros:
        for v in categorias['otros']:
            nombre_attr = getattr(v, 'label', None) or v.__name__
            if hasattr(grupo_otros, nombre_attr):
                valores_reducir.append(getattr(grupo_otros, nombre_attr))
    valores_validos_reducir = [v for v in valores_reducir if v > 0]
    promedio_reducir = np.mean(valores_validos_reducir) if valores_validos_reducir else 0

    inputs_sistema = {'aumentar': promedio_aumentar, 'reducir': promedio_reducir}

    salida, grados = ejecutar_sistema_difuso(reglas, 'coherenciaNutricionalMeta5', coherenciaNutricionalMeta5, inputs_sistema)

    conjuntoConMayorPertenencia = obtener_conjunto_mayor(grados)

    return {
        'porcentaje': salida,
        'nivel': nombresConjuntosOutput[conjuntoConMayorPertenencia]
    }