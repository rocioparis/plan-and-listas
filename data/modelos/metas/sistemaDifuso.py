from skfuzzy import control as controlDifuso

# ? Recibe, por ejemplo, las reglas, 'coherenciaNutricionalMeta1', coherenciaNutricionalMeta1 e inputs_sistema = {'aumentar': promedio e/ 10g y 20g (es 15), 'reducir': 0g}
def ejecutar_sistema_difuso(reglas, nombreSalida, variableSalida, inputs):
    from skfuzzy import interp_membership
    import traceback

    # Crea el sistema difuso a partir de las reglas definidas
    sistema = controlDifuso.ControlSystem(reglas)
    
    # Prepara la simulación para que tome los inputs y calcule la salida
    simulador = controlDifuso.ControlSystemSimulation(sistema)

    # ~ Cargar entradas a 'simulador'
    # Para cada variable, valor en (inputs_sistema o {} si no existe).items()
    # ? Ej: para 'aumentar', 15; 'reducir', 0
    for variable, valor in (inputs or {}).items():
        # Le da una input al simulador. Ese input tiene la variable. Le asigna un float, que es el valor o 0 si no existe
        # ? Ej: simulador.input['aumentar'] = float(15.0)
        # ? simulador.input['reducir'] = float(0)

        # ? Es decir, las entradas del simulador son:
        # ?? 'aumentar' = 15.0
        # ?? 'reducir' = 0
        simulador.input[variable] = float(valor or 0)

    # & (para depurar)
    print("variableSalida recibido:", id(variableSalida))
    print("Variables de salida del sistema:", sistema.consequents)
    print("Variables de entrada del sistema:", sistema.antecedents)

    try:
        simulador.compute()
    except Exception:
        print("⚠️ Error durante compute:")
        traceback.print_exc()

    # A la salida se le asigna el cálculo que hizo el simulador con las entradas
    # ^ "simulador.output' es un diccionario que guarda los resultados de las salidas después de que el simulador haya 
    # ^ hecho la evaluación.

    # * nombreSalida es un string, el nombre de la variable de salida (coherenciaNutricionalMeta1).
    # * .get() busca ese nombre en el diccionario simulador.output.
    # * Si por alguna razón no existe (no calculó nada o hubo un error), devuelve 0.0 por defecto.
    salida = float(simulador.output.get(nombreSalida, 0.0))

    # Calcular grados de pertenencia
    grados = {}
    # Para 'muyBaja' en coherenciaNutricionalMeta1, 'baja' en coherenciaNutricionalMeta1, etc.
    for etiqueta in variableSalida.terms:
        # mf = coherenciaNutricionalMeta1['baja'], etc.
        mf = variableSalida[etiqueta].mf
        # interp_membership sirve para calcular el grado de pertenencia de un valor a un conjunto difuso
        # # recibe: * variableSalida.universe: todos los posibles valores (0 al 100) que puede tomar coherenciaNutricionalMeta1
        # #         * mf: función de membresía de, por ejemplo coherenciaNutricionalMeta1['baja']
        # #         * salida: valor concreto del simulador (por ejemplo 40)   
        # ? grados['baja'] = float(interp_membership(universo de coherenciaNutricionalMeta1, 2, 40))
        grados[etiqueta] = float(interp_membership(variableSalida.universe, mf, salida))

    print("nombreSalida esperado:", nombreSalida)
    print("salida final:", salida)
    print("grados:", grados)

    # ? Devuelve 40, y grados para 'muyBaja', 'baja', etc.
    return salida, grados