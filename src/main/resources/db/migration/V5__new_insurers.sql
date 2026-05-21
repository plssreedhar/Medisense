-- Star Health policies
INSERT INTO policy_rules (policy_id, name, rules_json) VALUES
('starhealth-silver', 'Star Health Silver', '{
  "insurer": "Star Health",
  "tier": "Silver",
  "annual_limit": 300000,
  "room_rent_limit": 3000,
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_30_days", "post_hospitalization_60_days", "ambulance"],
  "excluded": ["outpatient_consultation", "pharmacy_opd", "dental", "vision", "cosmetic", "maternity", "pre_existing_conditions_first_2_years"],
  "copay_percent": 20,
  "sub_limits": {"icu": 5000, "surgery": 50000},
  "waiting_period_days": 30
}'),
('starhealth-gold', 'Star Health Gold', '{
  "insurer": "Star Health",
  "tier": "Gold",
  "annual_limit": 500000,
  "room_rent_limit": 6000,
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_60_days", "post_hospitalization_90_days", "ambulance", "maternity", "newborn_cover"],
  "excluded": ["outpatient_consultation", "dental", "vision", "cosmetic", "pre_existing_conditions_first_year"],
  "copay_percent": 10,
  "sub_limits": {"icu": 8000, "surgery": 100000, "maternity": 50000},
  "waiting_period_days": 30
}');

-- HDFC Ergo policies
INSERT INTO policy_rules (policy_id, name, rules_json) VALUES
('hdfcergo-optima', 'HDFC Ergo Optima Secure', '{
  "insurer": "HDFC Ergo",
  "tier": "Optima Secure",
  "annual_limit": 500000,
  "room_rent_limit": "no_cap",
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_60_days", "post_hospitalization_90_days", "ambulance", "organ_donor", "ayush_treatments"],
  "excluded": ["outpatient_consultation", "pharmacy_opd", "dental", "vision", "cosmetic", "obesity_treatment"],
  "copay_percent": 0,
  "sub_limits": {"icu": "no_cap", "surgery": "no_cap"},
  "waiting_period_days": 30,
  "restore_benefit": true
}'),
('hdfcergo-myhealth', 'HDFC Ergo my:health Suraksha', '{
  "insurer": "HDFC Ergo",
  "tier": "my:health Suraksha",
  "annual_limit": 200000,
  "room_rent_limit": 2000,
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_30_days", "post_hospitalization_60_days", "ambulance"],
  "excluded": ["outpatient_consultation", "pharmacy_opd", "dental", "vision", "cosmetic", "maternity", "pre_existing_conditions_first_3_years"],
  "copay_percent": 20,
  "sub_limits": {"icu": 3000, "surgery": 40000},
  "waiting_period_days": 30
}');

-- Niva Bupa policies
INSERT INTO policy_rules (policy_id, name, rules_json) VALUES
('nivabupa-reassure', 'Niva Bupa ReAssure 2.0', '{
  "insurer": "Niva Bupa",
  "tier": "ReAssure 2.0",
  "annual_limit": 1000000,
  "room_rent_limit": "no_cap",
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_60_days", "post_hospitalization_180_days", "ambulance", "maternity", "newborn_cover", "mental_health", "organ_donor", "ayush_treatments"],
  "excluded": ["outpatient_consultation", "dental_unless_accident", "vision", "cosmetic", "obesity_treatment"],
  "copay_percent": 0,
  "sub_limits": {"icu": "no_cap", "surgery": "no_cap", "maternity": 100000},
  "waiting_period_days": 30,
  "restore_benefit": true,
  "no_claim_bonus_percent": 50
}'),
('nivabupa-companion', 'Niva Bupa Health Companion', '{
  "insurer": "Niva Bupa",
  "tier": "Health Companion",
  "annual_limit": 300000,
  "room_rent_limit": 4000,
  "covered": ["inpatient_hospitalization", "day_care_procedures", "pre_hospitalization_30_days", "post_hospitalization_60_days", "ambulance"],
  "excluded": ["outpatient_consultation", "pharmacy_opd", "dental", "vision", "cosmetic", "maternity", "pre_existing_conditions_first_2_years"],
  "copay_percent": 10,
  "sub_limits": {"icu": 6000, "surgery": 75000},
  "waiting_period_days": 30
}');
