CREATE TABLE devices (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_devices PRIMARY KEY (id)
);

CREATE INDEX idx_devices_name ON devices(name);

CREATE TABLE device_sensors (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (device_id, sensor_id),
    CONSTRAINT fk_device_sensors_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_sensors_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_device_sensors_sensor_id ON device_sensors(sensor_id);
