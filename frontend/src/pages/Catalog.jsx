import { useState, useEffect } from "react";
import api, { CATALOG_API } from "../services/api";

export default function Catalog() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [category, setCategory] = useState("");

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const url = category
          ? `${CATALOG_API}/products?category=${category}`
          : `${CATALOG_API}/products`;
        const response = await api.get(url);
        setProducts(response.data);
        setError("");
      } catch {
        setError(
          "No se pudo conectar con el servicio de catálogo. ¿Está encendido el puerto 8082?",
        );
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [category]);

  return (
    <div className="max-w-6xl mx-auto py-6">
      <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <h1 className="text-3xl text-primary font-bold">Catálogo de Regalos</h1>

        {/* Filtros por categoría */}
        <div className="flex gap-2">
          <button
            onClick={() => setCategory("")}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${!category ? "bg-primary text-white" : "bg-white dark:bg-gray-800"}`}
          >
            Todos
          </button>
          <button
            onClick={() => setCategory("DESAYUNO")}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${category === "DESAYUNO" ? "bg-primary text-white" : "bg-white dark:bg-gray-800"}`}
          >
            Desayunos
          </button>
          <button
            onClick={() => setCategory("DECORACION")}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${category === "DECORACION" ? "bg-primary text-white" : "bg-white dark:bg-gray-800"}`}
          >
            Decoraciones
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-100 text-red-700 p-4 rounded-xl mb-6 text-center">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-20 text-gray-500">
          Cargando productos...
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-20 text-gray-500">
          No hay productos disponibles en esta categoría.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white dark:bg-gray-800 rounded-2xl shadow-md overflow-hidden hover:shadow-xl transition-shadow flex flex-col"
            >
              <div className="h-48 bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-gray-400">
                {/* Placeholder ilustrado si no hay imagen */}
                <span>🎁 {product.name}</span>
              </div>
              <div className="p-5 flex flex-col flex-grow">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-xs uppercase tracking-wider font-semibold px-2.5 py-1 bg-purple-100 dark:bg-purple-900 text-primary rounded-full">
                    {product.category}
                  </span>
                  <span
                    className={`text-xs px-2.5 py-1 rounded-full font-semibold ${product.stock > 0 ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}
                  >
                    {product.stock > 0
                      ? `Stock: ${product.stock}`
                      : "Sin stock"}
                  </span>
                </div>
                <h3 className="text-xl font-bold mb-2">{product.name}</h3>
                <div className="mt-auto flex justify-between items-center pt-4 border-t dark:border-gray-700">
                  <span className="text-2xl font-bold text-primary">
                    ${product.price}
                  </span>
                  <button
                    disabled={product.stock === 0}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${product.stock > 0 ? "bg-primary hover:bg-purple-700 text-white" : "bg-gray-300 cursor-not-allowed text-gray-500"}`}
                  >
                    Añadir a Wishlist
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}