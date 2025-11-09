from fastapi import APIRouter, HTTPException, Depends, Form
from sqlalchemy.orm import Session
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from pydantic import Field
import bcrypt
from typing import Annotated
from datetime import datetime
from datetime import date
from db import USERS
import re
from api_rest import get_db

router = APIRouter()

class UsuarioRegistro(BaseModel):
    username: Annotated[str, Field(min_length=5, max_length=15, pattern=r"^\S+$")]
    password: Annotated[str, Field(min_length=8, max_length=20)]
    password_conf: str
    fecha_nacimiento: str

password_regex = re.compile(r'^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#\$%^+=!])(?=\S+$).{8,20}$')

@router.post("/registrar")
async def registrarse(
    username:str = Form(...), 
    password:str = Form(...), 
    password_conf: str = Form(...),
    fechaNacimiento:str = Form(...),
    db: Session = Depends(get_db)
):
    # ~ VALIDACIONES ###############################################################################
    # ? Contraseña igual que su confirmación
    if password != password_conf:
        raise HTTPException(status_code=400, detail="Las contraseñas no coinciden")
    if not password_regex.match(password):
        raise HTTPException(status_code=400, detail="Formato de contraseña inválido")

    # ? Fecha de nacimiento
    # * Verificar si es mayor de edad
    def esMayor(fechaNacimiento: date):
        hoy = date.today()
        edad = hoy.year - fechaNacimiento.year - (
            (hoy.month, hoy.day) < (fechaNacimiento.month, fechaNacimiento.day)
        )
        return edad >= 18


    try:
        fecha = datetime.strptime(fechaNacimiento, "%d/%m/%Y").date()
        min_date = datetime.strptime("01/01/1925", "%d/%m/%Y").date()
        if fecha < min_date:
            raise HTTPException(status_code=400, detail="La fecha no se encuentra en el rango aceptado")
        if not esMayor(fecha):
            raise HTTPException(status_code=400, detail="Se debe ser mayor de edad para utilizar la aplicación")
        
    except ValueError:
        raise HTTPException(status_code=400, detail="Formato de fecha inválido")

    # ? Verificar si el usuario es existente
    if db.query(USERS).filter(USERS.username == username).first():
        raise HTTPException(status_code=400, detail="El nombre de usuario ya existe")

    # ? Hash de la contraseña
    password_bytes = password.encode('utf-8')

    salt = bcrypt.gensalt()
    password_hash = bcrypt.hashpw(password_bytes, salt).decode('utf-8')

    nuevo_usuario = USERS(
        username=username,
        password=password_hash,
        fechaNacimiento=datetime.strptime(fechaNacimiento, "%d/%m/%Y").date()
    )
    db.add(nuevo_usuario)
    db.commit()
    db.refresh(nuevo_usuario)

    return JSONResponse(
    status_code=201,
    content={
        "message": "Persona usuaria registrada correctamente",
        "usuario": {
            "username": username,
            "fecha_nacimiento": fechaNacimiento
        }
    }
)