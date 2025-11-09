def evaluar_metas(inputs, metas_modelos):
    resultados = {}
    for IDMeta in inputs.metas_seleccionadas:
        meta = metas_modelos.get(IDMeta)
        if not meta:
            continue
        coherencia = meta.ejecutar_sistema_difuso(inputs)
        resultados[IDMeta] = {
            "porcentaje": f"{coherencia['porcentaje']:.2f}%",
            "nivel": coherencia['nivel']
        }
    return resultados