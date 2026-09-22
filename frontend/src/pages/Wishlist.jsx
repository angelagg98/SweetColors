import { useState, useEffect, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import api, { WISHLIST_API } from "../services/api";

export default function Wishlist() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (user?.id) {
      fetchWishlist();
    } else {
      setLoading(false);
    }
  }, [user]);

  const fetchWishlist = async () => {
    try {
      setLoading(true);
      // El backend de wishlist requiere el userId como query param según la especificación
      const response = await api.get(`${WISHLIST_API}/items?userId=${user.id}`);
      setItems(response.data);
      setError("");
    } catch (err) {
      setError("No se pudo cargar la lista de deseos.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (itemId) => {
    try {
      await api.delete(`${WISHLIST_API}/items/${itemId}?userId=${user.id}`);
      setItems(items.filter((item) => item.id !== itemId));
    } catch (err) {
      alert("No se pudo eliminar el item");
    }
  };

  if (!user) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl text-primary font-bold mb-2">
          Inicia sesión para ver tu Wishlist
        </h2>
        <p className="text-gray-500">
          Debes estar autenticado para acceder a tus productos guardados.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-6">
      <h1 className="text-3xl text-primary font-bold mb-6">
        Mi Lista de Deseos
      </h1>

      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded-xl mb-6 text-center">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-20 text-gray-500">
          Cargando tu lista...
        </div>
      ) : items.length === 0 ? (
        <div className="text-center py-20 bg-white dark:bg-gray-800 rounded-2xl shadow-md">
          <p className="text-gray-500 text-lg">
            Tu lista de deseos está vacía.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {items.map((item) => (
            <div
              key={item.id}
              className="bg-white dark:bg-gray-800 p-5 rounded-2xl shadow-md flex justify-between items-center"
            >
              <div>
                <h3 className="text-lg font-bold">
                  Producto ID: {item.productId}
                </h3>
                <p className="text-gray-500 text-sm">
                  Cantidad: {item.quantity}
                </p>
              </div>
              <button
                onClick={() => handleDelete(item.id)}
                className="bg-red-100 hover:bg-red-200 text-red-700 px-4 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                Eliminar
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
