create table devices (
    id bigserial primary key,
    customer_id varchar(64) not null,
    device_id varchar(64) not null,
    first_seen_at timestamptz not null,
    last_seen_at timestamptz not null,
    trusted boolean not null,
    constraint uk_devices_customer_device unique (customer_id, device_id),
    constraint fk_devices_customer
        foreign key (customer_id)
        references customers (customer_id)
);

create index idx_devices_customer_device on devices (customer_id, device_id);
