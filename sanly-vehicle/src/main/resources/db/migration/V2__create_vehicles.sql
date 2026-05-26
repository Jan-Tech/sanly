CREATE TABLE plate_sequence (
    id          BIGINT PRIMARY KEY,
    last_number BIGINT NOT NULL DEFAULT 0
);
INSERT INTO plate_sequence (id, last_number) VALUES (1, 0);

CREATE TABLE vehicles (
    vehicle_id     BIGSERIAL PRIMARY KEY,
    plate_number   VARCHAR(12)  NOT NULL UNIQUE,
    vin            VARCHAR(17)  NOT NULL UNIQUE,
    make           VARCHAR(100) NOT NULL,
    model          VARCHAR(100) NOT NULL,
    year           INTEGER      NOT NULL,
    color          VARCHAR(50),
    engine_volume  VARCHAR(10),
    fuel_type      VARCHAR(20)  NOT NULL,
    vehicle_type   VARCHAR(30)  NOT NULL,
    registered_at  TIMESTAMP    NOT NULL DEFAULT now(),
    status         VARCHAR(20)  NOT NULL DEFAULT 'REGISTERED'
);

CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_make_model ON vehicles(make, model);
