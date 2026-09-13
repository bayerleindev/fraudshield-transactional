const snippets = {
  approve: `{
  "amount": 120.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "deviceStatus": "known_trusted",
  "beneficiaryStatus": "known",
  "customerAge": "older_than_7_days",
  "expectedDecision": "APPROVE",
  "expectedReasons": []
}`,
  review: `{
  "amount": 8500.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "deviceStatus": "new",
  "beneficiaryStatus": "new",
  "customerAge": "older_than_7_days",
  "expectedDecision": "REVIEW",
  "expectedReasons": [
    "HIGH_AMOUNT",
    "NEW_DEVICE",
    "NEW_BENEFICIARY"
  ]
}`,
  deny: `{
  "amount": 25000.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "deviceStatus": "new",
  "beneficiaryStatus": "new",
  "customerAge": "new_account",
  "expectedDecision": "DENY",
  "expectedReasons": [
    "VERY_HIGH_AMOUNT",
    "NEW_DEVICE",
    "NEW_BENEFICIARY",
    "NEW_ACCOUNT"
  ]
}`
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
