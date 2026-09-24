FROM node:20-alpine
 
WORKDIR /app
 
# Instala dependencias primero (aprovecha caché de Docker)
COPY package*.json ./
RUN npm install
 
# Copia el resto del código (en dev, el volumen del compose lo sobreescribe igual)
COPY . .
 
EXPOSE 5500
 
# --host 0.0.0.0 es obligatorio para que el servidor de Vite sea accesible
# desde fuera del contenedor (por defecto solo escucha en localhost del contenedor)
# --port 5500 coincide con el puerto configurado en vite.config.js
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5500"]
 