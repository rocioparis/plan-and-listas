import sys
import os

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

# & IMPORTACIONES ############################################################################################# 
from db import SessionLocal
from importaciones import FastAPI
from fastapi import APIRouter

# & PERMITIR LA COMUNICACIÓN ENTRE ARCHIVOS ###################################################################

sys.path.append(os.path.dirname(os.path.dirname(__file__)))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))

# & CREACIÓN DE LA API REST ###################################################################################

app = FastAPI(title="API REST Plan&Listas")

def get_db():
    db = SessionLocal()
    try: 
        yield db
    finally: 
        db.close()

# & IMPORTAR Y MONTAR LOS ROUTERS DE LOS MÓDULOS ########################################################
from api_rest import (
    usuarios,
    cuestionarios,
    metas,
    nutrientes,
    recetasPanel,
    login,
    rtados_bqda_rec,
    detalle_receta,
    plan_comida,
    lista_compra,
    agregar_receta,
    add_recipe,
    receta_por_plan,
    logout,
)

app.include_router(usuarios.router, prefix="/usuarios", tags=["Usuarios"])
app.include_router(login.router, tags=["Logs"])
app.include_router(cuestionarios.router, tags=["Cuestionarios"])
app.include_router(metas.router, tags=["Metas"])
app.include_router(nutrientes.router, tags=["Nutrientes"])
app.include_router(plan_comida.router)
app.include_router(lista_compra.router)
app.include_router(recetasPanel.router)
app.include_router(rtados_bqda_rec.router)
app.include_router(detalle_receta.router)
app.include_router(agregar_receta.router)
app.include_router(add_recipe.router)
app.include_router(receta_por_plan.router)
app.include_router(logout.router)

health_router = APIRouter()

@health_router.get("/health")
def health():
    db = SessionLocal()
    try:
        db.execute("SELECT 1")
        return {"status": "Base de datos activa"}
    except Exception as e:
        print(e)
        return {"status": "Error con la base de datos"}
    finally:
        db.close()

app.include_router(health_router)