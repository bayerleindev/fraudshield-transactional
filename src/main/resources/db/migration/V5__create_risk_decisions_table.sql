create table risk_decisions (
    id bigserial primary key,
    transaction_id varchar(64) not null,
    decision varchar(32) not null,
    score integer not null,
    rules_version varchar(32) not null,
    evaluated_at timestamptz not null,
    constraint fk_risk_decisions_transaction
        foreign key (transaction_id)
        references transactions (transaction_id),
    constraint chk_risk_decisions_decision check (decision in ('APPROVE', 'CHALLENGE', 'REVIEW', 'DENY')),
    constraint chk_risk_decisions_score_not_negative check (score >= 0),
    constraint chk_risk_decisions_rules_version_not_blank check (length(trim(rules_version)) > 0)
);

create index idx_risk_decisions_transaction_id on risk_decisions (transaction_id);
