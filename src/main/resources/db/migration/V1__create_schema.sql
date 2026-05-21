CREATE TABLE policy_rules (
    id          BIGSERIAL PRIMARY KEY,
    policy_id   VARCHAR(100) UNIQUE NOT NULL,
    name        VARCHAR(200) NOT NULL,
    rules_json  JSONB NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE claim_results (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(100),
    policy_id       VARCHAR(100),
    file_name       VARCHAR(500),
    local_path      VARCHAR(1000),
    patient_name_on_bill VARCHAR(255),
    verdict         VARCHAR(50),
    confidence      NUMERIC(5,2),
    rule_applied    VARCHAR(500),
    reason          TEXT,
    raw_llm_output  TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE mcp_tools (
    id               BIGSERIAL PRIMARY KEY,
    tool_name        VARCHAR(100) UNIQUE NOT NULL,
    tool_type        VARCHAR(50),
    description      TEXT,
    endpoint_config  JSONB,
    active           BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMPTZ DEFAULT NOW()
);
