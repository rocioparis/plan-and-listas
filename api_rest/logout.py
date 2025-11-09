from fastapi import HTTPException, APIRouter
from sqlalchemy.orm import Session
from fastapi.params import Depends
from db import SESIONES
from api_rest import get_db
from datetime import datetime
from pydantic import BaseModel

router = APIRouter()

class LogoutRequest(BaseModel):
    idUser: int

class LogoutResponse(BaseModel):
    success: bool
    message: str

@router.post("/logout/{id_user}", response_model=LogoutResponse)
def cerrar_sesion(id_user: int, db: Session = Depends(get_db)):
    sesion = db.query(SESIONES).filter(SESIONES.IDUser == id_user, SESIONES.horaFinalSesion == None).first()
    if not sesion:
        raise HTTPException(status_code=404, detail="No se encontró sesión activa")
    
    sesion.horaFinalSesion = datetime.now().time()
    db.commit()
    
    return LogoutResponse(success=True, message="Sesión cerrada correctamente")