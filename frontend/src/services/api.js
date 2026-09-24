import axios from "axios";

const api = axios.create({
  withCredentials: true,
});

// Todas las peticiones ahora pasan por el API Gateway (puerto 8080),
// que internamente rutea a cada microservicio. VITE_API_URL viene del
// docker-compose.yml; si no está definida (ej. corriendo el frontend
// fuera de Docker), usa localhost:8080 como valor por defecto.
const GATEWAY_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const AUTH_API = `${GATEWAY_URL}/api/auth`;
export const CATALOG_API = `${GATEWAY_URL}/api/catalog`;
export const WISHLIST_API = `${GATEWAY_URL}/api/wishlist`;
export const HISTORY_API = `${GATEWAY_URL}/api/history`;

export default api;