alter table transactions
    add column request_fingerprint varchar(64),
    add column first_evaluated_at timestamptz;

update transactions
set request_fingerprint = 'legacy-' || lpad(id::text, 57, '0'),
    first_evaluated_at = created_at
where request_fingerprint is null
   or first_evaluated_at is null;

alter table transactions
    alter column request_fingerprint set not null,
    alter column first_evaluated_at set not null,
    add constraint chk_transactions_request_fingerprint_not_blank check (length(trim(request_fingerprint)) > 0);
