CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    paid_by UUID NOT NULL REFERENCES users(id),
    group_id UUID NOT NULL REFERENCES groups(id),
    split_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expense_splits (
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    amount_owed NUMERIC(19, 2) NOT NULL
);