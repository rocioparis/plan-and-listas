def aplicarRestriccionesNutrientes(inputs, categorias, nutrientes_no_consumibles):
    if not nutrientes_no_consumibles:
        return # Función finaliza

    nutrientes_no_consumibles = [n.lower() for n in nutrientes_no_consumibles]

    for categoria, varsList in categorias.items():
        for v in varsList:
            nombre = getattr(v, "label", None)
            # Si el nombre del nutriente está marcado como no consumible por el usuario
            if nombre and nombre.lower() in nutrientes_no_consumibles:
                if hasattr(getattr(inputs, categoria), nombre): # ¿La categoría tiene el nutriente?
                    setattr(getattr(inputs, categoria), nombre, 0) # ¿Sí? Se le asigna 0