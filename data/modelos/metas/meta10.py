import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# ? AUMENTAR LA PRODUCTIVIDAD:
# * AUMENTAR: vitamina E, ácido fólico, 
# * calcio, hierro, magnesio, proteínas, 
# * fibra, carbohidratos, vitamina B2
# * vitamina B6, fósforo, potasio, sodio, 
# * cobre, zinc, vitamina A, manganeso
# ! REDUCIR: azúcar, exceso de vitamina A

from importaciones import controlDifuso, np, libreriaLD
from nombresConjuntosOutput import nombresConjuntosOutput, obtener_conjunto_mayor
from sistemaDifuso import ejecutar_sistema_difuso
from conjuntoSalida import crear_conjuntos_salida
from noConsumibles import aplicarRestriccionesNutrientes

from data.modelos.nutrientes.vitaminas import E, acidoFolico, B2, B6, A
from data.modelos.nutrientes.minerales import (Calcio, Hierro, Magnesio, Fosforo, Potasio, Sodio, Cobre, Zinc, Manganeso)
from data.modelos.nutrientes.macronutrientes import proteinas, Fibra, Carbohidratos, Azucar

vitaminas = [B2, B6, E, acidoFolico, A]
minerales = [Fosforo, Hierro, Magnesio, Calcio, Potasio, Manganeso, Cobre, Sodio, Zinc]
macronutrientes = [proteinas, Fibra, Carbohidratos]
evitables = [Azucar]

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

coherenciaNutricionalMeta10 = crear_conjuntos_salida('coherenciaNutricionalMeta10')
print("coherenciaNutricionalMeta10:", id(coherenciaNutricionalMeta10))

reglas = [
    controlDifuso.Rule(aumentar['mal'] & reducir['mal'], coherenciaNutricionalMeta10['muyBaja']),
    controlDifuso.Rule(aumentar['mal'] & reducir['medio'], coherenciaNutricionalMeta10['baja']),
    controlDifuso.Rule(aumentar['medio'] & reducir['bien'], coherenciaNutricionalMeta10['media']),
    controlDifuso.Rule(aumentar['bien'] & reducir['medio'], coherenciaNutricionalMeta10['alta']),
    controlDifuso.Rule(aumentar['bien'] & reducir['bien'], coherenciaNutricionalMeta10['muyAlta'])
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

    salida, grados = ejecutar_sistema_difuso(reglas, 'coherenciaNutricionalMeta10', coherenciaNutricionalMeta10, inputs_sistema)

    conjuntoConMayorPertenencia = obtener_conjunto_mayor(grados)

    return {
        'porcentaje': salida,
        'nivel': nombresConjuntosOutput[conjuntoConMayorPertenencia]
    }