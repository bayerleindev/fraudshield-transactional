create table customers (
    id bigserial primary key,
    customer_id varchar(64) not null,
    created_at timestamptz not null,
    last_password_change_at timestamptz,
    status varchar(32) not null,
    constraint uk_customers_customer_id unique (customer_id),
    constraint chk_customers_status_not_blank check (length(trim(status)) > 0)
);

create index idx_customers_customer_id on customers (customer_id);
