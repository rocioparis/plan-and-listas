import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# ? PROLONGAR LA VIDA:
# * AUMENTAR: carbohidratos, proteínas, 
# * vitamina B12, fibra, vitamina B2, 
# * vitamina B6, vitamina C, calcio, 
# * fósforo, magnesio, potasio, hierro, 
# * sodio, cobre, manganeso
# ! REDUCIR: grasas totales

from importaciones import controlDifuso, np, libreriaLD
from nombresConjuntosOutput import nombresConjuntosOutput, obtener_conjunto_mayor
from sistemaDifuso import ejecutar_sistema_difuso
from conjuntoSalida import crear_conjuntos_salida
from noConsumibles import aplicarRestriccionesNutrientes

from data.modelos.nutrientes.vitaminas import (B12, B2, B6, C)
from data.modelos.nutrientes.minerales import (Calcio, Fosforo, Magnesio, Potasio, Hierro, Sodio, Cobre, Manganeso)
from data.modelos.nutrientes.macronutrientes import Carbohidratos, proteinas, Fibra, grasasTotales

vitaminas = [B2, B6, C, B12]
minerales = [Fosforo, Hierro, Magnesio, Calcio, Potasio, Manganeso, Cobre, Sodio]
macronutrientes = [proteinas, Fibra, Carbohidratos]
evitables = [grasasTotales]

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

coherenciaNutricionalMeta9 = crear_conjuntos_salida('coherenciaNutricionalMeta9')
print("coherenciaNutricionalMeta9:", id(coherenciaNutricionalMeta9))

reglas = [
    controlDifuso.Rule(aumentar['mal'] & reducir['mal'], coherenciaNutricionalMeta9['muyBaja']),
    controlDifuso.Rule(aumentar['mal'] & reducir['medio'], coherenciaNutricionalMeta9['baja']),
    controlDifuso.Rule(aumentar['medio'] & reducir['bien'], coherenciaNutricionalMeta9['media']),
    controlDifuso.Rule(aumentar['bien'] & reducir['medio'], coherenciaNutricionalMeta9['alta']),
    controlDifuso.Rule(aumentar['bien'] & reducir['bien'], coherenciaNutricionalMeta9['muyAlta'])
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

    salida, grados = ejecutar_sistema_difuso(reglas, 'coherenciaNutricionalMeta9', coherenciaNutricionalMeta9, inputs_sistema)

    conjuntoConMayorPertenencia = obtener_conjunto_mayor(grados)

    return {
        'porcentaje': salida,
        'nivel': nombresConjuntosOutput[conjuntoConMayorPertenencia]
    }