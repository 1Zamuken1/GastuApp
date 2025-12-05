-- ============================================
-- CONSULTA PARA VER LOS CONCEPTOS EXISTENTES
-- ============================================
-- Ejecuta esta query primero para ver qué IDs tienes:

SELECT concepto_id, tipo, nombre, descripcion 
FROM concepto 
ORDER BY tipo, concepto_id;

-- ============================================
-- SCRIPT PARA CREAR CONCEPTOS (VERSIÓN CORREGIDA)
-- ============================================
-- Ejecutar este script para crear los conceptos

-- CONCEPTOS DE INGRESO
INSERT INTO concepto (tipo, nombre, descripcion) VALUES
('INGRESO', 'Salario', 'Ingreso mensual por trabajo'),
('INGRESO', 'Freelance', 'Trabajos independientes'),
('INGRESO', 'Inversiones', 'Rendimientos de inversiones'),
('INGRESO', 'Bonificación', 'Bonos y premios'),
('INGRESO', 'Venta', 'Venta de productos o servicios');

-- CONCEPTOS DE EGRESO
INSERT INTO concepto (tipo, nombre, descripcion) VALUES
('EGRESO', 'Alimentación', 'Compras de comida y restaurantes'),
('EGRESO', 'Transporte', 'Gasolina, transporte público, Uber'),
('EGRESO', 'Servicios', 'Luz, agua, internet, teléfono'),
('EGRESO', 'Entretenimiento', 'Cine, streaming, salidas'),
('EGRESO', 'Salud', 'Medicamentos, consultas médicas');

-- ============================================
-- DESPUÉS DE EJECUTAR, USA ESTOS IDs:
-- ============================================
-- Si insertaste en orden, los IDs serán:
-- INGRESOS: 1, 2, 3, 4, 5
-- EGRESOS: 6, 7, 8, 9, 10
--
-- Para crear un egreso, usa conceptoId: 6, 7, 8, 9 o 10
-- Para crear un ingreso, usa conceptoId: 1, 2, 3, 4 o 5

-- ============================================
-- SCRIPT PARA CREAR CONCEPTOS DE AHORRO
-- ============================================
-- INSERT INTO concepto (tipo, nombre, descripcion) VALUES
--('AHORRO', 'Vacaciones', 'Ahorro para viajes y vacaciones'),
--('AHORRO', 'Emergencias', 'Fondo de emergencia'),
--('AHORRO', 'Educación', 'Ahorro para estudios'),
--('AHORRO', 'Vivienda', 'Ahorro para compra de casa o apartamento'),
--('AHORRO', 'Vehículo', 'Ahorro para compra de vehículo'),
--('AHORRO', 'Inversión', 'Ahorro para inversiones'),
--('AHORRO', 'Jubilación', 'Ahorro para retiro'),
--('AHORRO', 'Tecnología', 'Ahorro para compra de tecnología'),
--('AHORRO', 'Salud', 'Ahorro para gastos médicos'),
--('AHORRO', 'Otro', 'Otros ahorros');
