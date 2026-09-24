import { Link } from "react-router-dom";

const offerings = [
  {
    icon: "🥞",
    title: "Desayunos sorpresa",
    text: "Armados con cariño para empezar el día de esa persona especial.",
  },
  {
    icon: "💝",
    title: "Detalles personalizados",
    text: "Regalos hechos a tu medida, con nombres, fechas y mensajes.",
  },
  {
    icon: "🧺",
    title: "Anchetas",
    text: "Combinaciones para cumpleaños, aniversarios y fechas especiales.",
  },
  {
    icon: "🥤",
    title: "Vasos personalizados y mágicos",
    text: "Diseños únicos, incluidos los vasos mágicos que sorprenden.",
  },
  {
    icon: "🎈",
    title: "Decoraciones",
    text: "Ambientes listos para celebrar, del globo al último detalle.",
  },
];

export default function Home() {
  return (
    <div className="max-w-5xl mx-auto">
      {/* Presentación */}
      <section className="text-center py-12 md:py-16">
        <h1 className="text-4xl md:text-6xl font-bold text-primary font-display">
          SweetColors
        </h1>
        <p className="mt-4 text-xl md:text-2xl font-medium">
          Regalos que se sienten hechos para ti
        </p>
        <p className="mt-4 max-w-2xl mx-auto text-gray-600 dark:text-gray-300">
          Creamos desayunos sorpresa, detalles personalizados, anchetas, vasos
          personalizados y decoraciones para que cada celebración se recuerde.
          Entra para ver el catálogo y guardar tus favoritos en tu lista de
          deseos.
        </p>
        <div className="mt-8 flex justify-center gap-3">
          <Link
            to="/login"
            className="bg-primary hover:bg-purple-700 text-white px-6 py-2.5 rounded-lg font-medium transition-colors"
          >
            Entrar
          </Link>
          <Link
            to="/register"
            className="bg-white dark:bg-gray-800 text-primary hover:bg-purple-50 px-6 py-2.5 rounded-lg font-medium transition-colors"
          >
            Registro
          </Link>
        </div>
      </section>

      {/* Qué hacemos */}
      <section className="py-6">
        <h2 className="text-2xl font-bold text-primary mb-6">Qué hacemos</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {offerings.map((item) => (
            <div
              key={item.title}
              className="bg-white dark:bg-gray-800 rounded-2xl shadow-md p-6"
            >
              <div className="text-3xl mb-3">{item.icon}</div>
              <h3 className="text-lg font-bold mb-1">{item.title}</h3>
              <p className="text-sm text-gray-600 dark:text-gray-300">
                {item.text}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Ubicación y contacto (solo visual por ahora) */}
      <section className="py-10 grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-md p-6">
          <h2 className="text-xl font-bold text-primary mb-3">Ubicación</h2>
          <p className="font-medium">📍 Tu ciudad, Colombia</p>
          <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
            Entregas a domicilio y recogida en tienda con cita previa.
          </p>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-md p-6">
          <h2 className="text-xl font-bold text-primary mb-3">Contacto</h2>
          <ul className="space-y-2">
            <li>💬 WhatsApp: +57 300 000 0000</li>
            <li>📸 Instagram: @sweetcolors</li>
            <li>✉️ hola@sweetcolors.com</li>
          </ul>
        </div>
      </section>
    </div>
  );
}