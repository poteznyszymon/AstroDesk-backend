ALTER TABLE network_devices DROP COLUMN IF EXISTS switch_name;
ALTER TABLE network_devices DROP COLUMN IF EXISTS switch_port;

ALTER TABLE network_history DROP COLUMN IF EXISTS switch_name;
ALTER TABLE network_history DROP COLUMN IF EXISTS switch_port;
