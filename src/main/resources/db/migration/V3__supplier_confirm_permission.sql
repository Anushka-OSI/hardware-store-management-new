-- V3: supplier PO confirm permission (supplier portal availability confirmation)

INSERT INTO permissions (code, name, module, description) VALUES
('po:confirm','Confirm PO Availability','PURCHASE','Supplier confirms availability of assigned POs')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Grant to SUPPLIER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code='po:confirm' WHERE r.name='SUPPLIER'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- Ensure ADMIN has it too (in case V2 cross-join predates this permission)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code='po:confirm' WHERE r.name='ADMIN'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- Link legacy supplier login to first supplier if not linked
UPDATE users SET supplier_id = (SELECT id FROM suppliers ORDER BY id LIMIT 1)
WHERE username='supplier@example.com' AND supplier_id IS NULL;
