## Ejecución
1. Crear entorno virtual e instalar dependencias:  
   `pip install -r requirements.txt`
2. Ejecutar:  
   `uvicorn api_rest:app --host 0.0.0.0 --port 8000 --reload`

## 🗄️ Base de datos

El proyecto usa **PostgreSQL**.  
Para probar la API localmente, crear una base de datos llamada `plan_listas` y configurar las variables de entorno en un archivo `.env` con los siguientes campos:

  DB_HOST=localhost
  DB_PORT=5432
  DB_NAME=plan_listas_ejemplo
  DB_USER=usuario_ejemplo
  DB_PASSWORD=pass_ejemplo

Luego ejecutar el backend.

## 🔒 Privacidad

Esta aplicación es un **producto mínimo viable (MVP) académico** que utiliza **datos sintéticos**.  
No se recolectan datos personales reales.  
Las contraseñas se almacenan de forma **hasheada** (bcrypt).  
Cualquier envío a servicios externos está desactivado.

## 🍴 Coherencia nutricional

Esta aplicación evalúa la coherencia entre las metas nutricionales de las personas con sus elecciones alimentarias. Para ello, se han utilizado como fuente las **Guías Alimentarias para la Población Argentina (GAPA)** (https://www.argentina.gob.ar/sites/default/files/bancos/2020-08/guias-alimentarias-para-la-poblacion-argentina_manual-de-aplicacion_0.pdf), un documento técnico-metodológico elaborado por múltiples entidades especializadas en nutrición. Complementariamente, se han tenido en cuenta libros acerca de salud y alimentación. Sin embargo, antes de utilizar la aplicación, se recomienda **consultar a un profesional de la salud**, ya que esta aplicación no proporciona ni reemplaza asesoramiento médico.

En los archivos A (api_rest/plan_comida.py (endpoint "/agregar")), B (data/modelos/metas/meta1.py), C (data/modelos/metas/noConsumibles.py), D (data/modelos/metas/nombresConjuntosOutput.py), E (data/modelos/metas/sistemaDifuso.py) y F (api_rest/recomendaciones.py) se ha comentado cómo es el *flujo* para **evaluar la coherencia nutricional cuando una persona planifica una comida**. Solo se ha comentado el ejemplo para la meta 1 (Llevar una dieta equilibrada).
Orden de visualización de los archivos: A, F, A, B, C, B, E, B, D, B, A
