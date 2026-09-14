create index idx_transactions_customer_id on transactions (customer_id);

create index idx_risk_decisions_transaction_evaluated_at on risk_decisions (transaction_id, evaluated_at desc);
