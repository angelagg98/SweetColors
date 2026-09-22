import axios from "axios";

const api = axios.create({
  withCredentials: true,
});

export const AUTH_API = "http://localhost:8081/api/auth";
export const CATALOG_API = "http://localhost:8082/api/catalog";
export const WISHLIST_API = "http://localhost:8083/api/wishlist";
export const HISTORY_API = "http://localhost:8084/api/history";

export default api;
