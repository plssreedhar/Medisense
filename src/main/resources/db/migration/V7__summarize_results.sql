CREATE TABLE summarize_results (
    id          BIGSERIAL PRIMARY KEY,
    session_id  VARCHAR(255),
    file_name   VARCHAR(500),
    local_path  VARCHAR(1000),
    summary     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
