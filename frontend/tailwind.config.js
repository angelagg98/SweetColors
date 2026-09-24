/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        // Colores que cambian entre día y noche (valores en src/index.css)
        primary: "rgb(var(--color-primary) / <alpha-value>)",
        secondary: "rgb(var(--color-secondary) / <alpha-value>)",
        accent: "rgb(var(--color-accent) / <alpha-value>)",
        success: "rgb(var(--color-success) / <alpha-value>)",

        // Fondo modo noche
        "dark-bg": "#0B0F1A",

        // Colores de apoyo de la paleta
        lila: "#C4B5FD",
        menta: "#A7F3D0",
        crema: "#FFF7ED",
        rosa: "#F472B6",
        "neon-purple": "#C084FC",
        "neon-blue": "#60A5FA",
        "neon-green": "#34D399",

        // Tarjetas y superficies en modo noche: tonos de azul oscuro
        // (reemplazan a gray-700 y gray-800 sin tocar cada componente)
        gray: {
          700: "#1E2540",
          800: "#131A2E",
        },
      },
      fontFamily: {
        display: ["Fredoka", "sans-serif"],
        body: ["Inter", "sans-serif"],
      },
    },
  },
  plugins: [],
};