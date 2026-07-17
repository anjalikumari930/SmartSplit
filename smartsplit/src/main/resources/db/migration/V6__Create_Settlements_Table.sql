CREATE TABLE IF NOT EXISTS settlements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    payer_id UUID NOT NULL,
    payee_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    settled_at TIMESTAMP,
    is_settled BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_settlement_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_settlement_payer FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_settlement_payee FOREIGN KEY (payee_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_settlements_group_created ON settlements(group_id, created_at DESC);
CREATE INDEX idx_settlements_is_settled ON settlements(is_settled);
CREATE INDEX idx_settlements_payer ON settlements(payer_id);
CREATE INDEX idx_settlements_payee ON settlements(payee_id);
