CREATE TABLE device (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    capabilities TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE device_sensor (
    device_id UUID NOT NULL,
    sensor_id UUID NOT NULL,
    PRIMARY KEY (device_id, sensor_id),
    CONSTRAINT fk_device FOREIGN KEY (device_id) REFERENCES sensor(id) ON DELETE CASCADE,
    CONSTRAINT fk_sensor FOREIGN KEY (sensor_id) REFERENCES sensor(id)
);