ALTER TABLE inventory DROP CONSTRAINT inventory_item_type_check;
ALTER TABLE inventory ADD CONSTRAINT inventory_item_type_check CHECK (item_type IN ('LAPTOP', 'KOMPUTER', 'DRUKARKA', 'ROUTER', 'SWITCH', 'TELEFON', 'SFP'));
