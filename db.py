from sqlalchemy import create_engine, MetaData, Column, ForeignKey, Integer, String, Date, Double, BigInteger, Boolean, DateTime
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.ext.automap import automap_base
from datetime import datetime

engine = create_engine(
    "postgresql+psycopg2://postgres:uePdo43eThNeg54Ujoug35JekoUz@localhost:5432/PlanListasBD"
)

metadata = MetaData(schema="public")
metadata.reflect(bind=engine)

AutoBase = automap_base(metadata=metadata)
AutoBase.prepare()

RECETAS_INGREDIENTES = AutoBase.classes.RECETAS_INGREDIENTES
SESIONES = AutoBase.classes.SESIONES

DeclarativeBase = declarative_base()

class LISTAS_PLANIFICACIONES(DeclarativeBase):
    __tablename__ = "LISTAS_PLANIFICACIONES"
    __table_args__ = {'schema': 'public'}

    IDPlanificacion = Column(Integer, ForeignKey("public.PLANIFICACIONES.IDPlanificacion"), primary_key=True)
    IDListaDeCompras = Column(Integer, ForeignKey("public.LISTAS_DE_COMPRAS.IDListaDeCompras"), primary_key=True)

class INGREDIENTES(DeclarativeBase):
    __tablename__ = "INGREDIENTES"
    __table_args__ = {'schema': 'public'}

    IDIngrediente = Column(Integer, primary_key=True)
    nombreIngrediente = Column(String)

class LISTAS_DE_COMPRAS(DeclarativeBase):
    __tablename__ = "LISTAS_DE_COMPRAS"
    __table_args__ = {'schema': 'public'}

    IDListaDeCompras = Column(Integer, primary_key=True)
    IDUser = Column(Integer, ForeignKey("public.USERS.IDUser"), nullable=False)
    fechaInicialLista = Column(Date)
    fechaFinalLista = Column(Date)
    fechaCreacion = Column(DateTime, default=datetime.utcnow)

class INGREDIENTES_LISTAS(DeclarativeBase):
    __tablename__ = "INGREDIENTES_LISTAS"
    __table_args__ = {'schema': 'public'}

    IDListaDeCompras = Column(Integer, ForeignKey("public.LISTAS_DE_COMPRAS.IDListaDeCompras"), primary_key=True)
    IDIngrediente = Column(Integer, ForeignKey("public.INGREDIENTES.IDIngrediente"), primary_key=True)
    disponibleItem = Column(Boolean)

class COMIDAS_DIARIAS(DeclarativeBase):
    __tablename__ = "COMIDAS_DIARIAS"
    __table_args__ = {'schema': 'public'}

    IDComida = Column(Integer, primary_key=True)
    nombreComida = Column(String)

class PLANIFICACIONES(DeclarativeBase):
    __tablename__ = "PLANIFICACIONES"
    __table_args__ = {'schema': 'public'}

    IDPlanificacion = Column(Integer, primary_key=True)
    IDUser = Column(Integer, ForeignKey("public.USERS.IDUser"), nullable=False)
    IDComida = Column(Integer, ForeignKey("public.COMIDAS_DIARIAS.IDComida"), nullable=True)
    fechaPlanificacion = Column(Date)
    numeroComensales = Column("numeroComensales", Integer)

class RECETAS(DeclarativeBase):
    __tablename__ = "RECETAS"
    __table_args__ = {'schema': 'public'}

    IDReceta = Column(Integer, primary_key=True)
    IDUser = Column(Integer, ForeignKey("public.USERS.IDUser"), nullable=True)
    nombreReceta = Column(String)
    imagenReceta = Column(String)
    procedimiento = Column(String)

class UNIDADES_MEDIDA(DeclarativeBase):
    __tablename__ = "UNIDADES_MEDIDA"
    __table_args__ = {'schema': 'public'}

    IDUnidadMedida = Column(Integer, primary_key=True)
    descripcionUM = Column(String)
    abreviacionUM = Column(String)

class CATEGORIAS_NUTRIENTES(DeclarativeBase):
    __tablename__ = "CATEGORIAS_NUTRIENTES"
    __table_args__ = {'schema': 'public'}

    IDCategoria = Column(Integer, primary_key=True)
    nombreCategoria = Column(String)

class NUTRIENTES(DeclarativeBase):
    __tablename__ = "NUTRIENTES"
    __table_args__ = {'schema': 'public'}

    IDNutriente = Column(Integer, primary_key=True)
    nombreNutriente = Column(String)
    IDUnidadMedida = Column(Integer, ForeignKey("public.UNIDADES_MEDIDA.IDUnidadMedida"), nullable=False)
    IDCategoria = Column(Integer, ForeignKey("public.CATEGORIAS_NUTRIENTES.IDCategoria"), nullable=False)

class USERS(DeclarativeBase):
    __tablename__ = "USERS"
    __table_args__ = {'schema': 'public'}

    IDUser = Column(Integer, primary_key=True)
    username = Column(String)
    password = Column(String)
    fechaNacimiento = Column(Date)

class ESTADOSCUES(DeclarativeBase):
    __tablename__ = "ESTADOSCUES"
    __table_args__ = {'schema': 'public'}

    IDEstadoT = Column(Integer, primary_key=True)
    nombreEstado = Column(String)

class CUESTIONARIOS(DeclarativeBase):
    __tablename__ = "CUESTIONARIOS"
    __table_args__ = {'schema': 'public'}

    IDCuestionario = Column(Integer, primary_key=True)
    IDUser = Column(Integer, ForeignKey("public.USERS.IDUser"), nullable=False)
    IDEstadoT = Column(Integer, ForeignKey("public.ESTADOSCUES.IDEstadoT"), nullable=True)

class METAS(DeclarativeBase):
    __tablename__ = "METAS"
    __table_args__ = {'schema': 'public'}

    IDMeta = Column(Integer, primary_key=True)
    nombreMeta = Column(String)

class METAS_CUESTIONARIOS(DeclarativeBase):
    __tablename__ = "METAS_CUESTIONARIOS"
    __table_args__ = {'schema': 'public'}

    IDCuestionario = Column(Integer, ForeignKey("public.CUESTIONARIOS.IDCuestionario"), primary_key=True)
    IDMeta = Column(Integer, ForeignKey("public.METAS.IDMeta"), primary_key=True)

class NUTRIENTES_CUESTIONARIOS(DeclarativeBase):
    __tablename__ = "NUTRIENTES_CUESTIONARIOS"
    __table_args__ = {'schema': 'public'}

    IDCuestionario = Column(Integer, ForeignKey("public.CUESTIONARIOS.IDCuestionario"), primary_key=True)
    IDNutriente = Column(Integer, ForeignKey("public.NUTRIENTES.IDNutriente"), primary_key=True)

class NUTRIENTES_RECETAS(DeclarativeBase):
    __tablename__ = "NUTRIENTES_RECETAS"
    __table_args__ = {'schema': 'public'}

    IDReceta = Column(Integer, ForeignKey("public.RECETAS.IDReceta"), primary_key=True)
    IDNutriente = Column(Integer, ForeignKey("public.NUTRIENTES.IDNutriente"), primary_key=True)
    valor = Column(Double)

class PLANIFICACIONES_RECETAS(DeclarativeBase):
    __tablename__ = "PLANIFICACIONES_RECETAS"
    __table_args__ = {'schema': 'public'}

    IDReceta = Column(Integer, ForeignKey("public.RECETAS.IDReceta"), primary_key=True)
    IDPlanificacion = Column(Integer, ForeignKey("public.PLANIFICACIONES.IDPlanificacion"), primary_key=True)


SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)