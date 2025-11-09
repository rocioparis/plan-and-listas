import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from importaciones import np, libreriaLD, controlDifuso

def crear_conjuntos_salida(nombre_variable):
    variable = controlDifuso.Consequent(np.arange(0, 101, 1), nombre_variable)
    variable['muyBaja'] = libreriaLD.trimf(variable.universe, [0, 0, 20])
    variable['baja'] = libreriaLD.trimf(variable.universe, [15, 25, 35])
    variable['media'] = libreriaLD.trimf(variable.universe, [30, 50, 65])
    variable['alta'] = libreriaLD.trimf(variable.universe, [60, 75, 85])
    variable['muyAlta'] = libreriaLD.trimf(variable.universe, [80, 100, 100])
    return variable