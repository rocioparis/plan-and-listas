import sys
import os
import numpy as np
import skfuzzy as libreriaLD
from skfuzzy import control as controlDifuso
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..')))