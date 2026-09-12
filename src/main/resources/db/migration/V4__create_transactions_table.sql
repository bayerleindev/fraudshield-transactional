create table transactions (
    id bigserial primary key,
    transaction_id varchar(64) not null,
    customer_id varchar(64) not null,
    amount numeric(19, 2) not null,
    currency char(3) not null,
    payment_method varchar(32) not null,
    beneficiary_id varchar(64) not null,
    device_id varchar(64) not null,
    ip_address varchar(45) not null,
    occurred_at timestamptz not null,
    created_at timestamptz not null,
    constraint uk_transactions_transaction_id unique (transaction_id),
    constraint fk_transactions_customer
        foreign key (customer_id)
        references customers (customer_id),
    constraint chk_transactions_amount_positive check (amount > 0),
    constraint chk_transactions_currency_iso_length check (length(currency) = 3),
    constraint chk_transactions_payment_method check (payment_method in ('PIX', 'CARD'))
);

create index idx_transactions_transaction_id on transactions (transaction_id);
