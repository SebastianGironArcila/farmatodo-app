CREATE TABLE IF NOT EXISTS client (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL
);


CREATE TABLE IF NOT EXISTS product (
   id VARCHAR(50) PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   description TEXT,
   price DECIMAL(10, 2) NOT NULL,
   stock INT NOT NULL
);

CREATE TABLE IF NOT EXISTS search_history (
   id VARCHAR(50) PRIMARY KEY,
   search_term VARCHAR(255) NOT NULL,
   search_timestamp TIMESTAMP NOT NULL
);


CREATE TABLE  IF NOT EXISTS shopping_cart (
    client_id VARCHAR(255) PRIMARY KEY,
    items TEXT NOT NULL,
    CONSTRAINT fk_client
        FOREIGN KEY(client_id)
            REFERENCES client(id)
            ON DELETE CASCADE
);

COMMENT ON COLUMN shopping_cart.client_id IS 'The ID of the client who owns the cart. Foreign key to the client table.';
COMMENT ON COLUMN shopping_cart.items IS 'A JSONB document containing the list of products (CartItem) in the cart.';





DELETE FROM product;

INSERT INTO product (id, name, description, price, stock) VALUES
('prod-001', 'Paracetamol 500mg', 'Caja con 20 tabletas para aliviar el dolor y la fiebre.', 2.50, 150),
('prod-002', 'Ibuprofeno 400mg', 'Caja con 30 tabletas, anti-inflamatorio.', 4.75, 80),
('prod-003', 'Vitamina C Efervescente', 'Tubo con 10 tabletas sabor naranja.', 3.20, 200),
('prod-004', 'Jarabe para la Tos', 'Frasco de 120ml para tos seca y con flema.', 6.00, 5),
('prod-005', 'Mascarillas Quirúrgicas', 'Paquete de 50 mascarillas desechables.', 15.50, 300),
('prod-006', 'Alcohol en Gel 250ml', 'Botella de alcohol antibacterial para manos.', 2.00, 2);
