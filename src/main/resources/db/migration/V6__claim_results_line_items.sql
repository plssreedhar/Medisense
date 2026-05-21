-- Add line_items column to store itemized verdict breakdown as JSON
ALTER TABLE claim_results
    ADD COLUMN IF NOT EXISTS line_items TEXT,
    ADD COLUMN IF NOT EXISTS claimable_amount NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12, 2);

COMMENT ON COLUMN claim_results.line_items IS
    'JSON array of {description, amount, verdict, reason} per bill line item';
COMMENT ON COLUMN claim_results.claimable_amount IS
    'Sum of amounts for CLAIMABLE line items';
COMMENT ON COLUMN claim_results.total_amount IS
    'Total bill amount parsed from the document';