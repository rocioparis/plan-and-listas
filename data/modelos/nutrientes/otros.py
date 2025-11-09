import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from importaciones import np, libreriaLD, controlDifuso

# ! Colesterol (--------MÁXIMO----- 300mg diarios)
# ^ 0.3 g

Colesterol = controlDifuso.Antecedent(np.arange(0,300,0.01), 'Colesterol')
Colesterol['deficiente'] = libreriaLD.trimf(Colesterol.universe, [0,0,90])
Colesterol['recomendado'] = libreriaLD.trimf(Colesterol.universe, [60,150,225])
Colesterol['excesivo'] = libreriaLD.trimf(Colesterol.universe, [180,300,300])

# ! Cafeína (-----máximo----- 400mg diarios)
# ^ 0.4 g

Cafeina = controlDifuso.Antecedent(np.arange(0,400,0.01), 'Cafeina')
Cafeina['deficiente'] = libreriaLD.trimf(Cafeina.universe, [0,0,120])
Cafeina['recomendado'] = libreriaLD.trimf(Cafeina.universe, [80,200,300])
Cafeina['excesivo'] = libreriaLD.trimf(Cafeina.universe, [240,400,400])