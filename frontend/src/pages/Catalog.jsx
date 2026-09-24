import { useState, useEffect, useContext } from "react";
import api, { CATALOG_API, WISHLIST_API } from "../services/api";
import { AuthContext } from "../context/AuthContext";

export default function Catalog() {
  const { user } = useContext(AuthContext);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [category, setCategory] = useState("");
  const [wishlistMsg, setWishlistMsg] = useState({});

  // --- Estado del formulario para crear productos ---
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: "",
    price: "",
    stock: "",
    category: "DESAYUNO",
  });
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);

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
useEffect(() => {
  // eslint-disable-next-line react-hooks/set-state-in-effect
  fetchProducts();
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [category]);

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateProduct = async (e) => {
    e.preventDefault();
    setFormError("");

    if (!form.name.trim()) {
      setFormError("El nombre es obligatorio.");
      return;
    }
    if (!form.price || Number(form.price) <= 0) {
      setFormError("El precio debe ser mayor a 0.");
      return;
    }
    if (form.stock === "" || Number(form.stock) < 0) {
      setFormError("El stock no puede ser negativo.");
      return;
    }

    try {
      setSubmitting(true);
      await api.post(`${CATALOG_API}/products`, {
        name: form.name.trim(),
        price: Number(form.price),
        stock: Number(form.stock),
        category: form.category,
      });
      setForm({ name: "", price: "", stock: "", category: "DESAYUNO" });
      setShowForm(false);
      await fetchProducts();
    } catch (err) {
      const backendMsg = err?.response?.data?.message;
      setFormError(
        backendMsg ||
          "No se pudo crear el producto. ¿Iniciaste sesión? Solo usuarios autenticados pueden crear productos.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddToWishlist = async (productId) => {
    if (!user?.id) {
      setWishlistMsg((prev) => ({
        ...prev,
        [productId]: { type: "error", text: "Inicia sesión para añadir a tu wishlist." },
      }));
      return;
    }

    try {
      await api.post(`${WISHLIST_API}/items`, {
        userId: user.id,
        productId,
        quantity: 1,
      });
      setWishlistMsg((prev) => ({
        ...prev,
        [productId]: { type: "success", text: "¡Añadido a tu wishlist!" },
      }));
    } catch (err) {
      const backendMsg = err?.response?.data?.message;
      setWishlistMsg((prev) => ({
        ...prev,
        [productId]: {
          type: "error",
          text: backendMsg || "No se pudo añadir a la wishlist.",
        },
      }));
    }
  };

  return (
    <div className="max-w-6xl mx-auto py-6">
      <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <h1 className="text-3xl text-primary font-bold">Catálogo de Regalos</h1>

        <div className="flex gap-2 items-center flex-wrap justify-center">
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
          <button
            onClick={() => setShowForm((prev) => !prev)}
            className="px-4 py-2 rounded-lg text-sm font-medium bg-green-600 hover:bg-green-700 text-white transition-colors"
          >
            {showForm ? "Cancelar" : "+ Nuevo producto"}
          </button>
        </div>
      </div>

      {showForm && (
        <form
          onSubmit={handleCreateProduct}
          className="bg-white dark:bg-gray-800 rounded-2xl shadow-md p-6 mb-8 grid grid-cols-1 md:grid-cols-4 gap-4 items-end"
        >
          <div className="md:col-span-2">
            <label className="block text-sm font-medium mb-1">Nombre</label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleFormChange}
              className="w-full border rounded-lg px-3 py-2 dark:bg-gray-700"
              placeholder="Ej: Desayuno sorpresa"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Precio</label>
            <input
              type="number"
              name="price"
              value={form.price}
              onChange={handleFormChange}
              className="w-full border rounded-lg px-3 py-2 dark:bg-gray-700"
              min="0"
              step="0.01"
              placeholder="45000"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Stock</label>
            <input
              type="number"
              name="stock"
              value={form.stock}
              onChange={handleFormChange}
              className="w-full border rounded-lg px-3 py-2 dark:bg-gray-700"
              min="0"
              placeholder="10"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Categoría</label>
            <select
              name="category"
              value={form.category}
              onChange={handleFormChange}
              className="w-full border rounded-lg px-3 py-2 dark:bg-gray-700"
            >
              <option value="DESAYUNO">Desayuno</option>
              <option value="DECORACION">Decoración</option>
            </select>
          </div>

          {formError && (
            <div className="md:col-span-4 text-red-600 text-sm">
              {formError}
            </div>
          )}

          <div className="md:col-span-4">
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 rounded-lg bg-primary hover:bg-purple-700 text-white font-medium disabled:opacity-50"
            >
              {submitting ? "Creando..." : "Crear producto"}
            </button>
          </div>
        </form>
      )}

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
                    onClick={() => handleAddToWishlist(product.id)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${product.stock > 0 ? "bg-primary hover:bg-purple-700 text-white" : "bg-gray-300 cursor-not-allowed text-gray-500"}`}
                  >
                    Añadir a Wishlist
                  </button>
                </div>
                {wishlistMsg[product.id] && (
                  <p
                    className={`text-sm mt-2 ${wishlistMsg[product.id].type === "success" ? "text-green-600" : "text-red-600"}`}
                  >
                    {wishlistMsg[product.id].text}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
