import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from importaciones import np, libreriaLD, controlDifuso

# ? Fuente: Guías Alimentarias para la Población Argentina (GAPA)
# ? 'recomendado' se refiere al valor recomendado diario

# * Proteínas (mínimo 75g diarios)

proteinas = controlDifuso.Antecedent(np.arange(0,131.25,0.01), 'proteinas')
proteinas['deficiente'] = libreriaLD.trimf(proteinas.universe, [0,0,45])
proteinas['recomendado'] = libreriaLD.trimf(proteinas.universe, [37.5,75,112.5])
proteinas['excesivo'] = libreriaLD.trimf(proteinas.universe, [93.75,131.25,131.25])

# * Grasas totales (mínimo 67g diarios)

grasasTotales = controlDifuso.Antecedent(np.arange(0,117.25,0.01), 'grasasTotales')
grasasTotales['deficiente'] = libreriaLD.trimf(grasasTotales.universe, [0,0,40.2])
grasasTotales['recomendado'] = libreriaLD.trimf(grasasTotales.universe, [33.5,67,100.5])
grasasTotales['excesivo'] = libreriaLD.trimf(grasasTotales.universe, [83.75,117.25,117.25])

# * Grasas saturadas (máximo 50g diarios)

grasasSaturadas = controlDifuso.Antecedent(np.arange(0,50,0.01), 'grasasSaturadas')
grasasSaturadas['deficiente'] = libreriaLD.trimf(grasasSaturadas.universe, [0,0,15])
grasasSaturadas['recomendado'] = libreriaLD.trimf(grasasSaturadas.universe, [10,25,35])
grasasSaturadas['excesivo'] = libreriaLD.trimf(grasasSaturadas.universe, [30,50,50])

# * Carbohidratos (mínimo 275g diarios)

Carbohidratos = controlDifuso.Antecedent(np.arange(0,481.25,0.01), 'Carbohidratos')
Carbohidratos['deficiente'] = libreriaLD.trimf(Carbohidratos.universe, [0,0,165])
Carbohidratos['recomendado'] = libreriaLD.trimf(Carbohidratos.universe, [137.5,275,412.5])
Carbohidratos['excesivo'] = libreriaLD.trimf(Carbohidratos.universe, [343.75,481.25,481.25])

# * Azúcar (máximo 50g diarios)

Azucar = controlDifuso.Antecedent(np.arange(0,50,0.01), 'Azucar')
Azucar['deficiente'] = libreriaLD.trimf(Azucar.universe, [0,0,15])
Azucar['recomendado'] = libreriaLD.trimf(Azucar.universe, [10,25,35])
Azucar['excesivo'] = libreriaLD.trimf(Azucar.universe, [30,50,50])

# * Fibra (mínimo 25g diarios)

Fibra = controlDifuso.Antecedent(np.arange(0,43.75,0.01), 'Fibra')
Fibra['deficiente'] = libreriaLD.trimf(Fibra.universe, [0,0,15])
Fibra['recomendado'] = libreriaLD.trimf(Fibra.universe, [12.5,25,37.5])
Fibra['excesivo'] = libreriaLD.trimf(Fibra.universe, [31.25,43.75,43.75])