CREATE TABLE IF NOT EXISTS policy_rules (
    id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    rules_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS claim_results (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    policy_id VARCHAR(50) NOT NULL,
    file_name VARCHAR(255),
    local_path TEXT,
    patient_name_on_bill VARCHAR(255),
    verdict VARCHAR(20),
    confidence NUMERIC(5,2),
    rule_applied VARCHAR(255),
    reason TEXT,
    raw_llm_output TEXT,
    created_at TIMESTAMP DEFAULT NOW()
    );

INSERT INTO policy_rules (policy_id, name, rules_json) VALUES
    ('medishield-silver', 'MediShield Silver', '{"inpatient":{"covered":true,"max_per_year":150000,"deductible":3000,"co_pay_percent":10},"outpatient":{"covered":true,"max_per_visit":2000,"excluded_items":["cosmetic","dental","vision"]},"pharmacy":{"covered":true,"max_per_month":5000,"excluded":["vitamins","supplements","OTC without prescription"]},"day_surgery":{"covered":true,"max_per_event":50000},"maternity":{"covered":false},"mental_health":{"covered":true,"max_per_year":20000},"pre_existing":{"covered":false,"waiting_period_months":24},"emergency":{"covered":true,"max_per_event":100000}}')
    ON CONFLICT (policy_id) DO NOTHING;

INSERT INTO policy_rules (policy_id, name, rules_json) VALUES
    ('medishield-gold', 'MediShield Gold', '{"inpatient":{"covered":true,"max_per_year":500000,"deductible":1000,"co_pay_percent":5},"outpatient":{"covered":true,"max_per_visit":5000,"excluded_items":["cosmetic"]},"pharmacy":{"covered":true,"max_per_month":15000,"excluded":["cosmetic supplements"]},"day_surgery":{"covered":true,"max_per_event":150000},"maternity":{"covered":true,"max_per_event":80000},"mental_health":{"covered":true,"max_per_year":60000},"pre_existing":{"covered":true,"waiting_period_months":12},"emergency":{"covered":true,"max_per_event":300000}}')
    ON CONFLICT (policy_id) DO NOTHING;