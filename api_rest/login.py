from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.orm import Session
from db import USERS, SESIONES
from pydantic import BaseModel
from datetime import datetime, timedelta
from api_rest import get_db
import bcrypt
from jose import jwt
from dotenv import load_dotenv
import os

load_dotenv()

router = APIRouter()

SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = "HS256"

class LoginRequest(BaseModel):
    username: str
    password: str

class UsuarioLoginData(BaseModel):
    id: int
    username: str

class LoginResponse(BaseModel):
    message: str
    usuario: UsuarioLoginData
    idSesion: int
    token: str

@router.post("/login", response_model=LoginResponse)
def login_user(request: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(USERS).filter(USERS.username == request.username).first()
    if not user:
        raise HTTPException(status_code=404, detail="Persona usuaria no registrada")
    
    password_bytes = request.password.encode('utf-8')
    hash_bytes = user.password.encode('utf-8')

    if not bcrypt.checkpw(password_bytes, hash_bytes):
        raise HTTPException(status_code=401, detail="Contraseña incorrecta")
    
    nueva_sesion = SESIONES(
        IDUser=user.IDUser,
        fechaSesion=datetime.today(),
        horaInicialSesion=datetime.now().time(),
        horaFinalSesion=None
    )
    db.add(nueva_sesion)
    db.commit()
    db.refresh(nueva_sesion)

    data_token = {
        "sub": user.username,
        "id": user.IDUser,
        "exp": datetime.utcnow() + timedelta(hours=3)
    }
    token = jwt.encode(data_token, SECRET_KEY, algorithm=ALGORITHM)

    return {
        "message": f"Ha iniciado sesión correctamente",
        "usuario": {
            "id": user.IDUser,
            "username": user.username
        },
        "idSesion": nueva_sesion.IDSesion,
        "token": token
    }