const snippets = {
  run: `docker compose up -d postgres
./gradlew bootRun`,
  seed: `docker exec fraudshield-postgres psql -U fraudshield -d fraudshield -c \\
  "insert into customers (customer_id, created_at, last_password_change_at, status)
   values ('cus-demo-001', now() - interval '30 days', null, 'ACTIVE')
   on conflict (customer_id) do nothing;"`,
  curl: `curl -i \\
  -H 'Content-Type: application/json' \\
  -H 'X-Correlation-Id: local-demo-001' \\
  -d '{
    "transactionId": "tx-demo-001",
    "customerId": "cus-demo-001",
    "amount": 8500.00,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "beneficiaryId": "ben-demo-001",
    "deviceId": "dev-demo-001",
    "ipAddress": "177.10.20.30",
    "occurredAt": "2026-09-12T14:30:00Z"
  }' \\
  http://localhost:8080/transactions/evaluate`
};

const snippet = document.querySelector("#snippet");
const tabs = document.querySelectorAll("[data-snippet]");

tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    tabs.forEach((item) => item.classList.remove("active"));
    tab.classList.add("active");
    snippet.textContent = snippets[tab.dataset.snippet];
  });
});
