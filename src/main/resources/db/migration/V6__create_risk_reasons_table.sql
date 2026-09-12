create table risk_reasons (
    id bigserial primary key,
    risk_decision_id bigint not null,
    code varchar(64) not null,
    description varchar(255) not null,
    score_impact integer not null,
    constraint fk_risk_reasons_decision
        foreign key (risk_decision_id)
        references risk_decisions (id)
        on delete cascade,
    constraint chk_risk_reasons_code check (
        code in (
            'HIGH_AMOUNT',
            'VERY_HIGH_AMOUNT',
            'NEW_DEVICE',
            'UNTRUSTED_DEVICE',
            'NEW_BENEFICIARY',
            'RECENT_PASSWORD_CHANGE',
            'NEW_ACCOUNT'
        )
    ),
    constraint chk_risk_reasons_description_not_blank check (length(trim(description)) > 0),
    constraint chk_risk_reasons_score_impact_positive check (score_impact > 0)
);
