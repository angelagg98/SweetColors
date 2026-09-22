/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        primary: "#8B5CF6",
        secondary: "#EADCC8",
        accent: "#3B82F6",
        success: "#4ADE80",
        "dark-bg": "#0B0F1A",
        "neon-purple": "#C084FC",
      },
      fontFamily: {
        display: ["Fredoka", "sans-serif"],
        body: ["Inter", "sans-serif"],
      },
    },
  },
  plugins: [],
};
