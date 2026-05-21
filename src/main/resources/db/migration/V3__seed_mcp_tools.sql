INSERT INTO mcp_tools (tool_name, tool_type, description, endpoint_config) VALUES
(
  'get_policy_rules',
  'POLICY_DB',
  'Retrieves policy rules JSON for a given policy_id from the database',
  '{"table": "policy_rules", "key_column": "policy_id", "value_column": "rules_json"}'
),
(
  'store_document',
  'DOC_STORE',
  'Stores an uploaded document to the configured storage backend',
  '{"backend": "local", "base_path": "./uploads"}'
);
