CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE group_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(255),
    CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);