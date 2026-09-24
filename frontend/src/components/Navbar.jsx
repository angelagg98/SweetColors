import { Link, useNavigate } from "react-router-dom";
import { useContext, useState } from "react";
import { AuthContext } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  // El tema inicial lo pone el script de index.html; aquí solo lo leemos
  const [dark, setDark] = useState(() =>
    document.documentElement.classList.contains("dark"),
  );

  const toggleTheme = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle("dark", next);
    try {
      localStorage.setItem("theme", next ? "dark" : "light");
    } catch {
      // sin acceso a localStorage: el cambio dura solo esta visita
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="bg-white dark:bg-gray-800 shadow-md">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-primary font-display">
          SweetColors
        </Link>
        <div className="flex items-center gap-6">
          {user && (
            <>
              <Link
                to="/"
                className="hover:text-primary transition-colors font-medium"
              >
                Catálogo
              </Link>
              <Link
                to="/wishlist"
                className="hover:text-primary transition-colors font-medium"
              >
                Mi Wishlist
              </Link>
            </>
          )}

          <button
            onClick={toggleTheme}
            aria-label={
              dark ? "Cambiar a modo día" : "Cambiar a modo noche"
            }
            title={dark ? "Modo día" : "Modo noche"}
            className="w-9 h-9 rounded-full bg-gray-100 dark:bg-gray-700 hover:bg-purple-100 dark:hover:bg-gray-700/70 flex items-center justify-center transition-colors"
          >
            <span aria-hidden="true">{dark ? "☀️" : "🌙"}</span>
          </button>

          {user ? (
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600 dark:text-gray-300">
                Hola, {user.fullName}
              </span>
              <button
                onClick={handleLogout}
                className="bg-red-50 dark:bg-red-950 text-red-600 dark:text-red-300 hover:bg-red-100 dark:hover:bg-red-900 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
              >
                Salir
              </button>
            </div>
          ) : (
            <div className="flex gap-3">
              <Link
                to="/login"
                className="px-4 py-1.5 text-primary font-medium hover:bg-purple-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                Entrar
              </Link>
              <Link
                to="/register"
                className="bg-primary hover:bg-purple-700 text-white px-4 py-1.5 rounded-lg font-medium transition-colors"
              >
                Registro
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}