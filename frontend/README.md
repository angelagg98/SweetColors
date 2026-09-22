# SweetColors — Frontend (React & Vite)

Este documento detalla la arquitectura, la estructura de carpetas, las tecnologías implementadas y los pasos exactos para configurar y ejecutar el cliente web de la plataforma e-commerce SweetColors, diseñado para comunicarse con los microservicios en Java Spring Boot.

1. Stack Tecnológico
- Core: React 18 + Vite.
- Enrutamiento: react-router-dom.
- Cliente HTTP: axios (configurado con withCredentials: true para las cookies HTTP-Only y tokens JWT del backend).
- Estilos: Tailwind CSS (Paleta de colores oficial: Morado #8B5CF6 y Fondo Beige #EADCC8).
- Tipografía: Google Fonts (Fredoka e Inter).
- Gestión de Sesión: Context API (AuthContext) and persistencia local.

2. Estructura de Carpetas del Proyecto
frontend/
├── src/
│   ├── components/
│   │   └── Navbar.jsx (Barra de navegación superior con control de sesión)
│   ├── context/
│   │   └── AuthContext.jsx (Proveedor de estado global de autenticación)
│   ├── pages/
│   │   ├── Catalog.jsx (Vista del catálogo con filtros por categorías)
│   │   ├── Login.jsx (Formulario de inicio de sesión conectado al auth-service)
│   │   ├── Register.jsx (Formulario de registro de nuevos usuarios)
│   │   └── Wishlist.jsx (Vista de la lista de deseos con gestión de productos)
│   ├── services/
│   │   └── api.js (Instancia global de Axios y endpoints por microservicio)
│   ├── App.jsx (Componente centralizador de rutas Router)
│   ├── index.css (Estilos base y directivas de Tailwind)
│   └── main.jsx (Punto de entrada de React)
├── tailwind.config.js (Configuración de colores y fuentes personalizadas)
├── postcss.config.js (Configuración de PostCSS)
└── package.json (Dependencias y scripts del proyecto)

3. Pantallas Implementadas
- Registro (/register): Permite dar de alta a usuarios con el auth-service (puerto 8081).
- Login (/login): Autentica al usuario manejando las credenciales y cookies de sesión.
- Catálogo (/): Muestra las tarjetas de productos del catalog-service (puerto 8082).
- Wishlist (/wishlist): Consume el wishlist-service (puerto 8083) para la lista de deseos.
- Navbar: Barra de navegación superior adaptativa según el estado de la sesión.

4. Instrucciones de Ejecución Paso a Paso
- Paso 1: Ubicarse en la carpeta del frontend
  cd frontend
- Paso 2: Instalar las dependencias del proyecto
  npm install
- Paso 3: Instalar librerías de soporte (Axios, Router y Tailwind)
  npm install axios react-router-dom
  npm install -D tailwindcss@3 postcss autoprefixer
- Paso 4: Arrancar el entorno de desarrollo local con Vite
  npm run dev

La aplicación quedará accesible en http://localhost:5173 o http://localhost:5174. Mantén activos los microservicios backend en sus puertos (8081, 8082, 8083, 8084) para pruebas de integración completas.