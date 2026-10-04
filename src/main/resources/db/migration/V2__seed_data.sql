-- GURUGE HARDWARE - V2 Seed Data

-- ROLES
INSERT INTO roles (name, description) VALUES
('ADMIN','Full system access'),
('INVENTORY_MANAGER','Products, stock, suppliers, POs'),
('CASHIER','POS, sales, receipts'),
('SUPPLIER','Restricted supplier portal')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- PERMISSIONS
INSERT INTO permissions (code, name, module, description) VALUES
('user:create','Create User','USER','Create staff accounts'),
('user:read','View Users','USER','View staff accounts'),
('user:update','Update User','USER','Edit staff details'),
('user:delete','Delete User','USER','Deactivate users'),
('user:role:assign','Assign Role','USER','Change roles'),
('product:create','Create Product','PRODUCT','Add products'),
('product:read','View Products','PRODUCT','View products'),
('product:update','Update Product','PRODUCT','Edit products'),
('product:delete','Delete Product','PRODUCT','Deactivate products'),
('product:price:update','Update Price','PRODUCT','Change prices'),
('category:manage','Manage Categories','PRODUCT','Add/edit categories'),
('brand:manage','Manage Brands','PRODUCT','Add/edit brands'),
('unit:manage','Manage Units','PRODUCT','Manage units'),
('inventory:read','View Inventory','INVENTORY','View stock'),
('inventory:adjust','Adjust Stock','INVENTORY','Manual adjustments'),
('inventory:transfer','Transfer Stock','INVENTORY','Stock transfer'),
('supplier:create','Create Supplier','SUPPLIER','Add suppliers'),
('supplier:read','View Suppliers','SUPPLIER','View suppliers'),
('supplier:update','Update Supplier','SUPPLIER','Edit suppliers'),
('supplier:delete','Delete Supplier','SUPPLIER','Deactivate suppliers'),
('po:create','Create PO','PURCHASE','Create purchase orders'),
('po:read','View POs','PURCHASE','View purchase orders'),
('po:update','Update PO','PURCHASE','Edit purchase orders'),
('po:send','Send PO','PURCHASE','Send PO to supplier'),
('po:receive','Receive PO','PURCHASE','Goods receiving'),
('po:cancel','Cancel PO','PURCHASE','Cancel PO'),
('pos:access','Access POS','SALE','Use POS terminal'),
('sale:create','Create Sale','SALE','Complete sales'),
('sale:read','View Sales','SALE','View sales history'),
('sale:return','Process Return','SALE','Handle returns'),
('sale:refund','Process Refund','SALE','Issue refunds'),
('report:sales','Sales Reports','REPORT','View sales reports'),
('report:inventory','Inventory Reports','REPORT','View inventory reports'),
('report:purchase','Purchase Reports','REPORT','View purchase reports'),
('report:payment','Payment Reports','REPORT','View payment reports'),
('request:read','View Requests','REQUEST','View customer requests'),
('request:respond','Respond Requests','REQUEST','Respond to requests'),
('request:close','Close Requests','REQUEST','Close requests'),
('audit:read','View Audit Logs','AUDIT','View audit trail'),
('config:manage','Manage Config','SYSTEM','System settings')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ROLE_PERMISSIONS
-- ADMIN = all
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name='ADMIN'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- INVENTORY_MANAGER subset
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
 'product:create','product:read','product:update','product:delete','product:price:update',
 'category:manage','brand:manage','unit:manage',
 'inventory:read','inventory:adjust','inventory:transfer',
 'supplier:create','supplier:read','supplier:update','supplier:delete',
 'po:create','po:read','po:update','po:send','po:receive','po:cancel',
 'sale:read','report:sales','report:inventory','report:purchase','report:payment',
 'request:read','request:respond','request:close'
) WHERE r.name='INVENTORY_MANAGER'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- CASHIER subset
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
 'product:read','inventory:read','pos:access','sale:create','sale:read','sale:return',
 'report:sales','report:payment'
) WHERE r.name='CASHIER'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- SUPPLIER subset (own data filtered in service layer)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
 'supplier:read','po:read','report:purchase'
) WHERE r.name='SUPPLIER'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- UNITS
INSERT INTO units (name, code, description) VALUES
('Piece','PCS','Single piece / each'),
('Box','BOX','Box'),
('Pack','PACK','Pack'),
('Meter','M','Meter length'),
('Kilogram','KG','Weight in kg'),
('Liter','L','Volume in liters'),
('Set','SET','Set of items')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- CATEGORIES
INSERT INTO categories (name, slug, description, sort_order) VALUES
('Cement','cement','Cement & concrete products',1),
('Paint','paint','Wall paints, enamels & primers',2),
('Electrical','electrical','Wires, switches, bulbs & fittings',3),
('Plumbing','plumbing','Pipes, taps, fittings & sanitary',4),
('Hardware Tools','hardware-tools','Hand tools, hammers, screwdrivers',5),
('Power Tools','power-tools','Drills, grinders, saws',6),
('Building Materials','building-materials','Bricks, sand, roofing sheets',7),
('Fasteners','fasteners','Nails, screws, bolts & nuts',8),
('Pipes','pipes','PVC, GI & HDPE pipes',9),
('Adhesives','adhesives','Glues, sealants & tapes',10),
('Safety Equipment','safety-equipment','Gloves, helmets, masks',11),
('Gardening','gardening','Garden tools & accessories',12),
('Roofing','roofing','Roofing sheets & accessories',13),
('Bathroom','bathroom','Bathroom fittings & accessories',14),
('Household','household','General household items',15)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- BRANDS
INSERT INTO brands (name, slug, description) VALUES
('Tokyo Cement','tokyo-cement','Tokyo Cement Lanka'),
('Bosch','bosch','Bosch Power Tools'),
('Multilac','multilac','Multilac Paints'),
('Orange','orange','Orange Electrical'),
('S-Lon','s-lon','S-Lon Pipes'),
('Stanley','stanley','Stanley Hand Tools'),
('Arpico','arpico','Arpico Products'),
('Dumex','dumex','General hardware')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- SUPPLIERS
INSERT INTO suppliers (company_name, contact_person, phone, email, address, tax_number, status, notes) VALUES
('ABC Hardware Suppliers','Mr. Perera','0771234567','abc@suppliers.lk','No. 45, Galle Road, Colombo 03','VAT123456','ACTIVE','Main cement & building materials supplier'),
('Lanka Electrical Distributors','Ms. Fernando','0777654321','lanka@electrical.lk','No. 12, Main Street, Negombo','VAT789012','ACTIVE','Electrical items & cables'),
('City Paints & Tools','Mr. Silva','0712345678','city@paints.lk','No. 88, Kandy Road, Colombo','VAT345678','ACTIVE','Paints and power tools')
ON DUPLICATE KEY UPDATE company_name=VALUES(company_name);

-- USERS (BCrypt cost 10)
-- Admin@123, Inventory@123, Cashier@123, Supplier@123
INSERT INTO users (employee_id, username, email, password_hash, full_name, phone, address, role_id, status) VALUES
('EMP-0001','admin@gurugehardware.lk','admin@gurugehardware.lk','$2b$10$ErqPIN8dWcrUcLgqh830oemWPbE1gaS.R4Qp5763uqJRCcASoF/ny','System Administrator','0770000001','Guruge Hardware, Colombo',(SELECT id FROM roles WHERE name='ADMIN'),'ACTIVE'),
('EMP-0002','inventory@gurugehardware.lk','inventory@gurugehardware.lk','$2b$10$m7lvEH2WH68eXBy0KOdoG.OxHEPlb3AgcIM3l.assKJYhmnoWVQdm','Inventory Manager','0770000002','Guruge Hardware, Colombo',(SELECT id FROM roles WHERE name='INVENTORY_MANAGER'),'ACTIVE'),
('EMP-0003','cashier@gurugehardware.lk','cashier@gurugehardware.lk','$2b$10$azgXigSSBQ06p.9v9xA72..Zcr.L7xR2pnKmIhb/vCCN1So1z7EWK','Cashier One','0770000003','Guruge Hardware, Colombo',(SELECT id FROM roles WHERE name='CASHIER'),'ACTIVE'),
('EMP-0004','supplier@example.com','supplier@example.com','$2b$10$RaShXQMYFx1cOqdsUOSraeXnIQ5yEKG/3YLtGg1vM8s5XZH257fRW','ABC Supplier User','0770000004','Colombo',(SELECT id FROM roles WHERE name='SUPPLIER'),'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Link supplier user to supplier 1
UPDATE users SET supplier_id = (SELECT id FROM suppliers LIMIT 1) WHERE username='supplier@example.com';

-- PRODUCTS (20 realistic items)
INSERT INTO products (sku, barcode, name, short_desc, full_desc, category_id, brand_id, unit_id, selling_price, cost_price, discount_percent, current_stock, min_stock_level, max_stock_level, status, image_url) VALUES
('CEM-50KG-001','4790001000011','Portland Cement 50kg','High quality Portland cement','Tokyo / local Portland cement 50kg bag for construction.',(SELECT id FROM categories WHERE slug='cement'),(SELECT id FROM brands WHERE slug='tokyo-cement'),(SELECT id FROM units WHERE code='PCS'),2250.00,1980.00,0,150,25,500,'ACTIVE',NULL),
('PVC-12-001','4790001000028','PVC Pipe 1/2 inch 10ft','Durable PVC pipe','S-Lon PVC pipe 1/2 inch, 10 feet length for plumbing.',(SELECT id FROM categories WHERE slug='pipes'),(SELECT id FROM brands WHERE slug='s-lon'),(SELECT id FROM units WHERE code='PCS'),1200.00,950.00,0,80,15,300,'ACTIVE',NULL),
('PVC-10-001','4790001000035','PVC Pipe 1 inch 10ft','Durable PVC pipe 1 inch','S-Lon PVC pipe 1 inch, 10 feet.',(SELECT id FROM categories WHERE slug='pipes'),(SELECT id FROM brands WHERE slug='s-lon'),(SELECT id FROM units WHERE code='PCS'),1850.00,1500.00,0,60,10,200,'ACTIVE',NULL),
('HAM-16OZ-001','4790001000042','Claw Hammer 16oz','Steel claw hammer','Stanley steel claw hammer 16oz with grip.',(SELECT id FROM categories WHERE slug='hardware-tools'),(SELECT id FROM brands WHERE slug='stanley'),(SELECT id FROM units WHERE code='PCS'),2850.00,2100.00,5,40,5,100,'ACTIVE',NULL),
('SCR-SET-001','4790001000059','Screwdriver Set 6pcs','6pc screwdriver set','6-piece precision screwdriver set.',(SELECT id FROM categories WHERE slug='hardware-tools'),(SELECT id FROM brands WHERE slug='stanley'),(SELECT id FROM units WHERE code='SET'),3200.00,2400.00,0,35,5,80,'ACTIVE',NULL),
('DRL-750W-001','4790001000066','Electric Drill 750W Bosch','Powerful 750W drill','Bosch 750W impact drill with chuck set.',(SELECT id FROM categories WHERE slug='power-tools'),(SELECT id FROM brands WHERE slug='bosch'),(SELECT id FROM units WHERE code='PCS'),24500.00,19800.00,0,12,3,30,'ACTIVE',NULL),
('GRD-850W-001','4790001000073','Angle Grinder 850W','850W angle grinder','Bosch 4 inch angle grinder 850W.',(SELECT id FROM categories WHERE slug='power-tools'),(SELECT id FROM brands WHERE slug='bosch'),(SELECT id FROM units WHERE code='PCS'),18900.00,15200.00,3,10,2,25,'ACTIVE',NULL),
('PNT-1L-WHT','4790001000080','Wall Paint White 1L','Premium emulsion 1L','Multilac emulsion wall paint white 1L.',(SELECT id FROM categories WHERE slug='paint'),(SELECT id FROM brands WHERE slug='multilac'),(SELECT id FROM units WHERE code='L'),2950.00,2300.00,0,50,10,150,'ACTIVE',NULL),
('PNT-4L-WHT','4790001000097','Wall Paint White 4L','Premium emulsion 4L','Multilac emulsion wall paint white 4L.',(SELECT id FROM categories WHERE slug='paint'),(SELECT id FROM brands WHERE slug='multilac'),(SELECT id FROM units WHERE code='L'),9850.00,7900.00,2,30,5,80,'ACTIVE',NULL),
('CBL-2P5-001','4790001000103','Electrical Cable 2.5mm 100m','Copper cable roll','Orange copper electrical cable 2.5mm 100m coil.',(SELECT id FROM categories WHERE slug='electrical'),(SELECT id FROM brands WHERE slug='orange'),(SELECT id FROM units WHERE code='M'),185.00,140.00,0,500,50,2000,'ACTIVE',NULL),
('LED-9W-001','4790001000110','LED Bulb 9W','Energy saving LED','Orange 9W LED bulb B22.',(SELECT id FROM categories WHERE slug='electrical'),(SELECT id FROM brands WHERE slug='orange'),(SELECT id FROM units WHERE code='PCS'),850.00,620.00,0,200,20,500,'ACTIVE',NULL),
('TAP-12-001','4790001000127','Water Tap 1/2 inch','Brass water tap','Chrome brass water tap 1/2 inch.',(SELECT id FROM categories WHERE slug='plumbing'),(SELECT id FROM brands WHERE slug='dumex'),(SELECT id FROM units WHERE code='PCS'),2450.00,1800.00,0,45,8,120,'ACTIVE',NULL),
('LOCK-DR-001','4790001000134','Door Lock Cylinder','Secure door lock','Heavy duty door lock with 3 keys.',(SELECT id FROM categories WHERE slug='hardware-tools'),(SELECT id FROM brands WHERE slug='dumex'),(SELECT id FROM units WHERE code='PCS'),3900.00,2950.00,0,28,5,70,'ACTIVE',NULL),
('ROOF-12FT-001','4790001000141','Roofing Sheet 12ft','Zinc roofing sheet','Corrugated roofing sheet 12ft.',(SELECT id FROM categories WHERE slug='roofing'),(SELECT id FROM brands WHERE slug='arpico'),(SELECT id FROM units WHERE code='PCS'),6850.00,5600.00,0,40,8,150,'ACTIVE',NULL),
('NAIL-1KG-2IN','4790001000158','Wire Nails 2 inch 1kg','Steel nails','2 inch wire nails 1kg pack.',(SELECT id FROM categories WHERE slug='fasteners'),(SELECT id FROM brands WHERE slug='dumex'),(SELECT id FROM units WHERE code='KG'),680.00,480.00,0,100,20,400,'ACTIVE',NULL),
('SCRW-WD-001','4790001000165','Wood Screws 1.5 inch 100pcs','Wood screws box','Wood screws 1.5 inch, box of 100.',(SELECT id FROM categories WHERE slug='fasteners'),(SELECT id FROM brands WHERE slug='dumex'),(SELECT id FROM units WHERE code='BOX'),950.00,680.00,0,70,10,200,'ACTIVE',NULL),
('ADH-50G-001','4790001000172','Super Glue 50g','Fast adhesive','Industrial super glue 50g tube.',(SELECT id FROM categories WHERE slug='adhesives'),(SELECT id FROM brands WHERE slug='dumex'),(SELECT id FROM units WHERE code='PCS'),550.00,380.00,0,120,15,300,'ACTIVE',NULL),
('GLV-SFT-001','4790001000189','Safety Gloves','Work gloves','Heavy duty safety work gloves.',(SELECT id FROM categories WHERE slug='safety-equipment'),(SELECT id FROM brands WHERE slug='stanley'),(SELECT id FROM units WHERE code='PCS'),750.00,520.00,0,90,10,250,'ACTIVE',NULL),
('HELM-YLW-001','4790001000196','Safety Helmet Yellow','Construction helmet','Yellow safety helmet adjustable.',(SELECT id FROM categories WHERE slug='safety-equipment'),(SELECT id FROM brands WHERE slug='stanley'),(SELECT id FROM units WHERE code='PCS'),1850.00,1350.00,0,5,10,60,'ACTIVE',NULL),
('TAPE-PTFE-001','4790001000202','PTFE Thread Seal Tape','Plumbing tape','PTFE white thread seal tape 12mm.',(SELECT id FROM categories WHERE slug='adhesives'),(SELECT id FROM brands WHERE slug='s-lon'),(SELECT id FROM units WHERE code='PCS'),180.00,110.00,0,0,20,500,'ACTIVE',NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- OPENING STOCK TRANSACTIONS for each product
INSERT INTO inventory_transactions (product_id, quantity, previous_stock, new_stock, movement_type, reference_type, reason, user_id)
SELECT id, current_stock, 0, current_stock, 'OPENING_STOCK', 'SEED', 'Initial seed stock', (SELECT id FROM users WHERE username='admin@gurugehardware.lk')
FROM products
WHERE NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE movement_type='OPENING_STOCK' LIMIT 1);
