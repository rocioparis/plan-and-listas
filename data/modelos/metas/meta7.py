import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# ? FAVORECER EL FUNCIONAMIENTO CEREBRAL:
# * AUMENTAR: vitamina A, vitamina B12, 
# * vitamina B1, vitamina B2, vitamina B3, 
# * vitamina B6, ácido fólico, fibra, 
# * potasio, sodio, fósforo, proteínas, 
# * calcio, magnesio, carbohidratos, 
# * hierro, manganeso, zinc, cobre, selenio
# ! REDUCIR: azúcar, exceso de sodio, grasas saturadas

from importaciones import controlDifuso, np, libreriaLD
from nombresConjuntosOutput import nombresConjuntosOutput, obtener_conjunto_mayor
from sistemaDifuso import ejecutar_sistema_difuso
from conjuntoSalida import crear_conjuntos_salida
from noConsumibles import aplicarRestriccionesNutrientes

from data.modelos.nutrientes.vitaminas import (A, B12, B1, B2, B3, B6, acidoFolico)
from data.modelos.nutrientes.minerales import (Potasio, Sodio, Fosforo, Calcio, Magnesio, Hierro, Manganeso, Zinc, Cobre,
Selenio)
from data.modelos.nutrientes.macronutrientes import Fibra, proteinas, Carbohidratos, Azucar, grasasSaturadas

vitaminas = [B12, B1, B2, B3, B6, A, acidoFolico]
minerales = [Fosforo, Hierro, Magnesio, Calcio, Selenio, Zinc, Potasio, Sodio, Manganeso, Cobre]
macronutrientes = [proteinas, Fibra, Carbohidratos]
evitables = [Azucar, grasasSaturadas]

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

coherenciaNutricionalMeta7 = crear_conjuntos_salida('coherenciaNutricionalMeta7')
print("coherenciaNutricionalMeta7:", id(coherenciaNutricionalMeta7))

reglas = [
    controlDifuso.Rule(aumentar['mal'] & reducir['mal'], coherenciaNutricionalMeta7['muyBaja']),
    controlDifuso.Rule(aumentar['mal'] & reducir['medio'], coherenciaNutricionalMeta7['baja']),
    controlDifuso.Rule(aumentar['medio'] & reducir['bien'], coherenciaNutricionalMeta7['media']),
    controlDifuso.Rule(aumentar['bien'] & reducir['medio'], coherenciaNutricionalMeta7['alta']),
    controlDifuso.Rule(aumentar['bien'] & reducir['bien'], coherenciaNutricionalMeta7['muyAlta'])
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
    promedio_aumentar = np.nan_to_num(np.mean(valores_aumentar), nan=0)

    valores_reducir = []
    grupo_otros = getattr(inputs, 'otros', None)
    if grupo_otros:
        for v in categorias['otros']:
            nombre_attr = getattr(v, 'label', None) or v.__name__
            if hasattr(grupo_otros, nombre_attr):
                valores_reducir.append(getattr(grupo_otros, nombre_attr))
    promedio_reducir = np.nan_to_num(np.mean(valores_reducir), nan=0)

    inputs_sistema = {'aumentar': promedio_aumentar, 'reducir': promedio_reducir}

    salida, grados = ejecutar_sistema_difuso(reglas, 'coherenciaNutricionalMeta7', coherenciaNutricionalMeta7, inputs_sistema)

    conjuntoConMayorPertenencia = obtener_conjunto_mayor(grados)

    return {
        'porcentaje': salida,
        'nivel': nombresConjuntosOutput[conjuntoConMayorPertenencia]
    }