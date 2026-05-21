-- Chat sessions: one row per conversation
CREATE TABLE chat_sessions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_name VARCHAR(255),
    patient_name VARCHAR(255),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Chat messages: each turn in a session
CREATE TABLE chat_messages (
    id             BIGSERIAL PRIMARY KEY,
    session_id     UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role           VARCHAR(16)  NOT NULL CHECK (role IN ('user', 'assistant')),
    content        TEXT         NOT NULL,
    content_type   VARCHAR(32)  NOT NULL DEFAULT 'text'
                     CHECK (content_type IN ('text', 'summary', 'verdict', 'question')),
    attached_file  VARCHAR(512),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id);

-- Session state: tracks mid-conversation context (insurer selection, uploaded PDF, etc.)
CREATE TABLE chat_session_state (
    session_id      UUID PRIMARY KEY REFERENCES chat_sessions(id) ON DELETE CASCADE,
    doc_type        VARCHAR(32),
    extracted_json  TEXT,
    insurer_id      VARCHAR(64),
    policy_tier     VARCHAR(64),
    awaiting        VARCHAR(64),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
