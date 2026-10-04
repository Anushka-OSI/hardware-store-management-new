-- V4: supplier PO reject permission (supplier declines a SENT PO with reason)

INSERT INTO permissions (code, name, module, description) VALUES
('po:reject','Reject PO','PURCHASE','Supplier rejects an assigned SENT PO with a reason')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Grant to SUPPLIER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code='po:reject' WHERE r.name='SUPPLIER'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- Ensure ADMIN has it too
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code='po:reject' WHERE r.name='ADMIN'
ON DUPLICATE KEY UPDATE role_id=role_id;
