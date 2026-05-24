-- V7__network_open_ports.sql
-- Lista otwartych portów wykrytych przez scanner, format CSV: "22/ssh,80/http,443/https"
ALTER TABLE network_devices
    ADD COLUMN open_ports VARCHAR(500);
