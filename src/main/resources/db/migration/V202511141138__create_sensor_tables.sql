CREATE TABLE sensors (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sensors PRIMARY KEY (id)
);

CREATE TABLE sensor_capabilities (
    sensor_id BIGINT NOT NULL,
    capability VARCHAR(255) NOT NULL,
    PRIMARY KEY (sensor_id, capability),
    CONSTRAINT fk_sensor_capabilities_sensors FOREIGN KEY (sensor_id) 
        REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_sensors_name ON sensors(name);
CREATE INDEX idx_sensors_type ON sensors(type);
CREATE INDEX idx_sensor_capabilities_sensor_id ON sensor_capabilities(sensor_id);
