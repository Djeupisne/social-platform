FROM python:3.12-slim

# Dépendances système
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Installer les dépendances Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copier le code source
COPY app/ ./app/

EXPOSE 8083

# Lancement avec uvicorn (équivalent du java -jar)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8083"]
