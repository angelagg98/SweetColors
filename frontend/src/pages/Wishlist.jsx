import { useState, useEffect, useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import api, { CATALOG_API, WISHLIST_API } from "../services/api";

const formatPrice = (value) =>
  Number(value || 0).toLocaleString("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  });

export default function Wishlist() {
  const [items, setItems] = useState([]);
  const [products, setProducts] = useState({}); // { [productId]: producto }
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const { user } = useContext(AuthContext);

  const userId = user?.id;

  useEffect(() => {
    if (!userId) return;

    let cancelled = false;

    const loadWishlist = async () => {
      try {
        // 1. Items de la wishlist (solo traen productId y quantity)
        const wishlistRes = await api.get(
          `${WISHLIST_API}/items?userId=${userId}`,
        );

        // 2. Datos completos de los productos desde el catálogo
        let byId = {};
        try {
          const productsRes = await api.get(`${CATALOG_API}/products`);
          productsRes.data.forEach((p) => {
            byId[p.id] = p;
          });
        } catch {
          // Si el catálogo falla, igual mostramos la lista con lo básico
          byId = {};
        }

        if (cancelled) return;
        setItems(wishlistRes.data);
        setProducts(byId);
        setError("");
      } catch {
        if (!cancelled) setError("No se pudo cargar la lista de deseos.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadWishlist();

    return () => {
      cancelled = true;
    };
  }, [userId]);

  const handleDelete = async (itemId) => {
    try {
      await api.delete(`${WISHLIST_API}/items/${itemId}?userId=${user.id}`);
      setItems(items.filter((item) => item.id !== itemId));
    } catch {
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

  const totalUnits = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce((sum, item) => {
    const product = products[item.productId];
    return sum + (product ? product.price * item.quantity : 0);
  }, 0);

  return (
    <div className="max-w-4xl mx-auto py-6">
      <div className="flex justify-between items-end mb-6">
        <h1 className="text-3xl text-primary font-bold">Mi Lista de Deseos</h1>
        {!loading && items.length > 0 && (
          <span className="text-sm text-gray-500">
            {items.length} {items.length === 1 ? "producto" : "productos"} ·{" "}
            {totalUnits} {totalUnits === 1 ? "unidad" : "unidades"}
          </span>
        )}
      </div>

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
        <>
          <div className="space-y-4">
            {items.map((item) => {
              const product = products[item.productId];
              const subtotal = product ? product.price * item.quantity : null;

              return (
                <div
                  key={item.id}
                  className="bg-white dark:bg-gray-800 rounded-2xl shadow-md overflow-hidden flex flex-col sm:flex-row"
                >
                  {/* Imagen (placeholder igual que en el catálogo) */}
                  <div className="sm:w-40 h-32 sm:h-auto bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-gray-400 text-3xl shrink-0">
                    🎁
                  </div>

                  <div className="p-5 flex-grow flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                      {product ? (
                        <>
                          <span className="text-xs uppercase tracking-wider font-semibold px-2.5 py-1 bg-purple-100 dark:bg-purple-900 text-primary rounded-full">
                            {product.category}
                          </span>
                          <h3 className="text-xl font-bold mt-2">
                            {product.name}
                          </h3>
                          <p className="text-gray-500 text-sm mt-1">
                            {formatPrice(product.price)} c/u
                          </p>
                        </>
                      ) : (
                        <>
                          <h3 className="text-xl font-bold">
                            Producto #{item.productId}
                          </h3>
                          <p className="text-gray-500 text-sm mt-1">
                            No se pudo cargar la información de este producto.
                          </p>
                        </>
                      )}
                      <span className="inline-block mt-3 text-sm font-semibold px-3 py-1 rounded-full bg-gray-100 dark:bg-gray-700">
                        Cantidad: {item.quantity}
                      </span>
                    </div>

                    <div className="flex sm:flex-col items-center sm:items-end justify-between gap-3">
                      {subtotal !== null && (
                        <span className="text-2xl font-bold text-primary">
                          {formatPrice(subtotal)}
                        </span>
                      )}
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="bg-red-100 hover:bg-red-200 text-red-700 px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                      >
                        Eliminar
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Total */}
          <div className="mt-6 bg-white dark:bg-gray-800 rounded-2xl shadow-md p-5 flex justify-between items-center">
            <span className="text-lg font-medium">Total de tu lista</span>
            <span className="text-2xl font-bold text-primary">
              {formatPrice(totalPrice)}
            </span>
          </div>
        </>
      )}
    </div>
  );
}