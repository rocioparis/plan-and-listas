import sys, os

sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

from importaciones import np, libreriaLD, controlDifuso

# & Vitamina A (según las guías alimentarias GAPA, se recomiendan mínimo 700mcg diarios)

A = controlDifuso.Antecedent(np.arange(0,1500,0.01), 'A')
A['deficiente'] = libreriaLD.trimf(A.universe, [0,0,400])
A['recomendado'] = libreriaLD.trimf(A.universe, [300,700,1100])
A['excesivo'] = libreriaLD.trimf(A.universe, [900,1500,1500])

# & Betacaroteno (mínimo 700mcg diarios). Es un tipo de vitamina A, pero en la literatura se habla de ella
# & independientemente.

Betacaroteno = controlDifuso.Antecedent(np.arange(0,1500,0.01), 'Betacaroteno')
Betacaroteno['deficiente'] = libreriaLD.trimf(Betacaroteno.universe, [0,0,400])
Betacaroteno['recomendado'] = libreriaLD.trimf(Betacaroteno.universe, [300,700,1100])
Betacaroteno['excesivo'] = libreriaLD.trimf(Betacaroteno.universe, [900,1500,1500])

# & Vitamina B1 (mínimo 1.1mg diarios)

B1 = controlDifuso.Antecedent(np.arange(0,3,0.01), 'B1')
B1['deficiente'] = libreriaLD.trimf(B1.universe, [0,0,0.7])
B1['recomendado'] = libreriaLD.trimf(B1.universe, [0.6,1.1,1.6])
B1['excesivo'] = libreriaLD.trimf(B1.universe, [1.3,3,3])

# & Vitamina B2 (mínimo 1.1mg diarios)

B2 = controlDifuso.Antecedent(np.arange(0,3,0.01), 'B2')
B2['deficiente'] = libreriaLD.trimf(B2.universe, [0,0,0.7])
B2['recomendado'] = libreriaLD.trimf(B2.universe, [0.6,1.1,1.6])
B2['excesivo'] = libreriaLD.trimf(B2.universe, [1.3,3,3])

# & Vitamina B3 (mínimo 14mg diarios)

B3 = controlDifuso.Antecedent(np.arange(0,30,0.1), 'B3')
B3['deficiente'] = libreriaLD.trimf(B3.universe, [0,0,7])
B3['recomendado'] = libreriaLD.trimf(B3.universe, [6,14,22])
B3['excesivo'] = libreriaLD.trimf(B3.universe, [18,30,30])

# & Vitamina B6 (mínimo 1.3mg diarios)

B6 = controlDifuso.Antecedent(np.arange(0,3,0.01), 'B6')
B6['deficiente'] = libreriaLD.trimf(B6.universe, [0,0,0.7])
B6['recomendado'] = libreriaLD.trimf(B6.universe, [0.6,1.3,2])
B6['excesivo'] = libreriaLD.trimf(B6.universe, [1.6,3,3])

# & Vitamina B12 (mínimo 2.4mcg diarios)

B12 = controlDifuso.Antecedent(np.arange(0,10,0.01), 'B12')
B12['deficiente'] = libreriaLD.trimf(B12.universe, [0,0,1]) 
B12['recomendado'] = libreriaLD.trimf(B12.universe, [1,2.4,4])
B12['excesivo'] = libreriaLD.trimf(B12.universe, [3,10,10]) 

# & Vitamina C (mínimo 75mg diarios)

C = controlDifuso.Antecedent(np.arange(0,200,0.01), 'C')
C['deficiente'] = libreriaLD.trimf(C.universe, [0,0,40])
C['recomendado'] = libreriaLD.trimf(C.universe, [30,75,120])
C['excesivo'] = libreriaLD.trimf(C.universe, [100,200,200])

# & Vitamina D (mínimo 15mcg diarios)

D = controlDifuso.Antecedent(np.arange(0,50,0.01), 'D')
D['deficiente'] = libreriaLD.trimf(D.universe, [0,0,7])
D['recomendado'] = libreriaLD.trimf(D.universe, [5,15,25])
D['excesivo'] = libreriaLD.trimf(D.universe, [20,50,50])

# & Vitamina E (mínimo 15mg diarios)

E = controlDifuso.Antecedent(np.arange(0,50,0.01), 'E')
E['deficiente'] = libreriaLD.trimf(E.universe, [0,0,7])
E['recomendado'] = libreriaLD.trimf(E.universe, [5,15,25])
E['excesivo'] = libreriaLD.trimf(E.universe, [20,50,50])

# & Vitamina K (mínimo 57.96mcg diarios)

K = controlDifuso.Antecedent(np.arange(0,200,0.01), 'K')
K['deficiente'] = libreriaLD.trimf(K.universe, [0,0,30])
K['recomendado'] = libreriaLD.trimf(K.universe, [20,58,100])
K['excesivo'] = libreriaLD.trimf(K.universe, [80,200,200])

# & Ácido fólico (mínimo 400mcg diarios)

acidoFolico = controlDifuso.Antecedent(np.arange(0,1000,0.01), 'acidoFolico')
acidoFolico['deficiente'] = libreriaLD.trimf(acidoFolico.universe, [0,0,200])
acidoFolico['recomendado'] = libreriaLD.trimf(acidoFolico.universe, [150,400,600])
acidoFolico['excesivo'] = libreriaLD.trimf(acidoFolico.universe, [500,1000,1000])