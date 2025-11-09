nombresConjuntosOutput = {
    'muyBaja' : 'MUY BAJA',
    'baja' : 'BAJA',
    'media' : 'MEDIA',
    'alta' : 'ALTA',
    'muyAlta' : 'MUY ALTA'
} # Para mostrar algo legible al usuario

# ? par. de entrada 'muyBaja' = 0, 'baja' = 2, etc.
def obtener_conjunto_mayor(grados):

    # ? conjuntoConMayorPertenencia = media
    conjuntoConMayorPertenencia = max(grados, key=grados.get)
    return conjuntoConMayorPertenencia