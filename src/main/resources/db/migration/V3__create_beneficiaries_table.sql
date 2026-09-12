create table beneficiaries (
    id bigserial primary key,
    customer_id varchar(64) not null,
    beneficiary_id varchar(64) not null,
    first_transaction_at timestamptz not null,
    trusted boolean not null,
    constraint uk_beneficiaries_customer_beneficiary unique (customer_id, beneficiary_id),
    constraint fk_beneficiaries_customer
        foreign key (customer_id)
        references customers (customer_id)
);

create index idx_beneficiaries_customer_beneficiary on beneficiaries (customer_id, beneficiary_id);
