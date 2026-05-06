-- V3__network_module.sql
-- Network devices table
CREATE TABLE network_devices (
    id               BIGSERIAL PRIMARY KEY,
    mac_address      VARCHAR(17)  NOT NULL UNIQUE,
    ip_address       VARCHAR(45),
    hostname         VARCHAR(255),
    vendor           VARCHAR(100),
    switch_name      VARCHAR(100),
    switch_port      VARCHAR(30),
    last_seen_at     TIMESTAMPTZ  NOT NULL,
    is_imported      BOOLEAN      NOT NULL DEFAULT FALSE,
    linked_asset_id  BIGINT,
    linked_asset_name VARCHAR(255)
);

CREATE INDEX idx_network_devices_vendor      ON network_devices (vendor);
CREATE INDEX idx_network_devices_imported    ON network_devices (is_imported);
CREATE INDEX idx_network_devices_last_seen   ON network_devices (last_seen_at DESC);

-- Network history table
CREATE TABLE network_history (
    id          BIGSERIAL PRIMARY KEY,
    device_id   BIGINT       NOT NULL REFERENCES network_devices (id) ON DELETE CASCADE,
    mac_address VARCHAR(17)  NOT NULL,
    ip_address  VARCHAR(45),
    switch_name VARCHAR(100),
    switch_port VARCHAR(30),
    seen_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_network_history_mac     ON network_history (mac_address);
CREATE INDEX idx_network_history_seen_at ON network_history (seen_at DESC);
