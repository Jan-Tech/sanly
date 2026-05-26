CREATE TABLE customs_ports (
    port_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    port_code  VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(200) NOT NULL,
    port_type  VARCHAR(30)  NOT NULL,
    region     VARCHAR(100),
    address    VARCHAR(300),
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_port_status ON customs_ports(status);
CREATE INDEX idx_port_type   ON customs_ports(port_type);
