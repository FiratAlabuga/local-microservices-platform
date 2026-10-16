CREATE TABLE IF NOT EXISTS notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id   UUID        NOT NULL UNIQUE,
    order_id   UUID        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PROCESSED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_order_id        ON notifications(order_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status          ON notifications(status);
