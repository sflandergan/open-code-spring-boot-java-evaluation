-- V202511140900__create_devices_table.sql
CREATE TABLE devices (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- V202511140901__create_device_sensor_relationships_table.sql
CREATE TABLE device_sensor_relationships (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (sensor_id) REFERENCES sensors(id),
    PRIMARY KEY (device_id, sensor_id)
);