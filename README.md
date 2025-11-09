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
Las contraseñas se almacenan de forma **hasheada**.  
Cualquier envío a servicios externos está desactivado en este entorno de demo.
