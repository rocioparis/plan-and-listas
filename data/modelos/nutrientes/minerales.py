import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from importaciones import np, libreriaLD, controlDifuso

# ? Hierro (mínimo 18mg diarios)

Hierro = controlDifuso.Antecedent(np.arange(0,25,1), 'Hierro')
Hierro['deficiente'] = libreriaLD.trimf(Hierro.universe, [0,0,0.01])
Hierro['recomendado'] = libreriaLD.trimf(Hierro.universe, [9,18,22])
Hierro['excesivo'] = libreriaLD.trimf(Hierro.universe, [20,25,25])

# ? Calcio (mínimo 1000mg diarios)

Calcio = controlDifuso.Antecedent(np.arange(0,1700,1), 'Calcio')
Calcio['deficiente'] = libreriaLD.trimf(Calcio.universe, [0,0,0.01])
Calcio['recomendado'] = libreriaLD.trimf(Calcio.universe, [500,1000,1500])
Calcio['excesivo'] = libreriaLD.trimf(Calcio.universe, [1300,1700,1700])

# ? Magnesio (mínimo 1.8mg diarios)

Magnesio = controlDifuso.Antecedent(np.arange(0,3.15,0.01), 'Magnesio')
Magnesio['deficiente'] = libreriaLD.trimf(Magnesio.universe, [0,0,1.08])
Magnesio['recomendado'] = libreriaLD.trimf(Magnesio.universe, [0.9,1.8,2.7])
Magnesio['excesivo'] = libreriaLD.trimf(Magnesio.universe, [2.25,3.15,3.15])

# ? Zinc (mínimo 8mg diarios)

Zinc = controlDifuso.Antecedent(np.arange(0,14,0.01), 'Zinc')
Zinc['deficiente'] = libreriaLD.trimf(Zinc.universe, [0,0,3])
Zinc['recomendado'] = libreriaLD.trimf(Zinc.universe, [4,8,12])
Zinc['excesivo'] = libreriaLD.trimf(Zinc.universe, [10,14,14])

# ? Selenio (mínimo 55mcg diarios)

Selenio = controlDifuso.Antecedent(np.arange(0,96.25,0.01), 'Selenio')
Selenio['deficiente'] = libreriaLD.trimf(Selenio.universe, [0,0,33])
Selenio['recomendado'] = libreriaLD.trimf(Selenio.universe, [27.5,55,82.5])
Selenio['excesivo'] = libreriaLD.trimf(Selenio.universe, [68.75,96.25,96.25])

# ? Fósforo (mínimo 700mg diarios)

Fosforo = controlDifuso.Antecedent(np.arange(0,1500,0.01), 'Fosforo')
Fosforo['deficiente'] = libreriaLD.trimf(Fosforo.universe, [0,0,400])
Fosforo['recomendado'] = libreriaLD.trimf(Fosforo.universe, [300,700,1100])
Fosforo['excesivo'] = libreriaLD.trimf(Fosforo.universe, [900,1500,1500])

# ? Sodio (mínimo 1.5g diarios)

Sodio = controlDifuso.Antecedent(np.arange(0,2.625,0.01), 'Sodio')
Sodio['deficiente'] = libreriaLD.trimf(Sodio.universe, [0,0,0.91])
Sodio['recomendado'] = libreriaLD.trimf(Sodio.universe, [0.75,1.5,2.25])
Sodio['excesivo'] = libreriaLD.trimf(Sodio.universe, [1.875,2.625,2.625])

# ? Yodo (mínimo 150mcg diarios)

Yodo = controlDifuso.Antecedent(np.arange(0,262.5,0.01), 'Yodo')
Yodo['deficiente'] = libreriaLD.trimf(Yodo.universe, [0,0,90])
Yodo['recomendado'] = libreriaLD.trimf(Yodo.universe, [75,150,225])
Yodo['excesivo'] = libreriaLD.trimf(Yodo.universe, [187.5,262.5,262.5])

# ? Potasio (mínimo 4.7g diarios)

Potasio = controlDifuso.Antecedent(np.arange(0,8.225,0.01), 'Potasio')
Potasio['deficiente'] = libreriaLD.trimf(Potasio.universe, [0,0,2.82])
Potasio['recomendado'] = libreriaLD.trimf(Potasio.universe, [2.35,4.7,7.05])
Potasio['excesivo'] = libreriaLD.trimf(Potasio.universe, [5.875,8.225,8.225])

# ? Manganeso (mínimo 1.8mg diarios)

Manganeso = controlDifuso.Antecedent(np.arange(0,3.15,0.01), 'Manganeso')
Manganeso['deficiente'] = libreriaLD.trimf(Manganeso.universe, [0,0,1.08])
Manganeso['recomendado'] = libreriaLD.trimf(Manganeso.universe, [0.9,1.8,2.7])
Manganeso['excesivo'] = libreriaLD.trimf(Manganeso.universe, [2.25,3.15,3.15])

# ? Boro (mínimo 0.5mg diarios)

Boro = controlDifuso.Antecedent(np.arange(0,0.875,0.01), 'Boro')
Boro['deficiente'] = libreriaLD.trimf(Boro.universe, [0,0,0.3])
Boro['recomendado'] = libreriaLD.trimf(Boro.universe, [0.25,0.5,0.75])
Boro['excesivo'] = libreriaLD.trimf(Boro.universe, [0.625,0.875,0.875])

# ? Cobre (mínimo 900mcg diarios)

Cobre = controlDifuso.Antecedent(np.arange(0,1575,0.01), 'Cobre')
Cobre['deficiente'] = libreriaLD.trimf(Cobre.universe, [0,0,540])
Cobre['recomendado'] = libreriaLD.trimf(Cobre.universe, [450,900,1350])
Cobre['excesivo'] = libreriaLD.trimf(Cobre.universe, [1125,1575,1575])