-- SweetColors - Productos de ejemplo (datos simulados, precios en COP)
USE catalog_db;

INSERT INTO products (name, price, stock, category, created_at) VALUES
  ('Desayuno clasico',          45000.00,  0, 'DESAYUNO',   NOW(6)),
  ('Desayuno Fitness',          55000.00,  5, 'DESAYUNO',   NOW(6)),
  ('Desayuno sorpresa premium', 75000.00,  8, 'DESAYUNO',   NOW(6)),
  ('Desayuno de cumpleanos',    68000.00, 12, 'DESAYUNO',   NOW(6)),
  ('Desayuno romantico',        62000.00,  6, 'DESAYUNO',   NOW(6)),
  ('Decoracion con globos',     90000.00, 10, 'DECORACION', NOW(6)),
  ('Decoracion de aniversario', 120000.00, 4, 'DECORACION', NOW(6)),
  ('Arco de globos',            85000.00,  0, 'DECORACION', NOW(6)),
  ('Decoracion baby shower',   110000.00,  3, 'DECORACION', NOW(6)),
  ('Decoracion tematica',      130000.00,  7, 'DECORACION', NOW(6));
