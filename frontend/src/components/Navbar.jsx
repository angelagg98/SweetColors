import { Link, useNavigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

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

          {user ? (
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600 dark:text-gray-300">
                Hola, {user.fullName}
              </span>
              <button
                onClick={handleLogout}
                className="bg-red-50 text-red-600 hover:bg-red-100 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors"
              >
                Salir
              </button>
            </div>
          ) : (
            <div className="flex gap-3">
              <Link
                to="/login"
                className="px-4 py-1.5 text-primary font-medium hover:bg-purple-50 rounded-lg transition-colors"
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