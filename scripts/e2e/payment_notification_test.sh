#!/usr/bin/env bash
set -euo pipefail

# Simple E2E script: create order -> create payment -> assert notification appears
# Requirements: docker compose services running, jq installed

API_GATEWAY="http://localhost:8082/api/v1"
TICKET_SVC="http://localhost:8083/api/v1"
USER_SVC="http://localhost:8081/api/v1"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required for this script. Install jq and retry." >&2
  exit 2
fi

# Get token (if TOKEN env not provided)
if [ -z "${TOKEN-}" ]; then
  echo "Obtaining auth token..."
  TOKEN=$(curl -s -X POST "$USER_SVC/auth/login" -H 'Content-Type: application/json' -d '{"email":"controller@test.com","password":"test123"}' | jq -r .token)
  if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "Failed to obtain token. Check user service is up and credentials." >&2
    exit 3
  fi
fi

echo "Using token: ${TOKEN:0:10}..."

# Create an order for userId 21
echo "Creating order..."
# validFrom must be at least 2 hours in the future per service validation
VALID_FROM=$(date -d '+3 hours' +"%Y-%m-%dT%H:%M:%S")
VALID_UNTIL=$(date -d '+5 hours' +"%Y-%m-%dT%H:%M:%S")

ORDER_PAYLOAD=$(jq -n --arg uid "21" --arg vf "$VALID_FROM" --arg vu "$VALID_UNTIL" '{userId:($uid|tonumber), tickets:[{ticketType:"SINGLE", routeId:1, validFrom:$vf, validUntil:$vu}]}')

ORDER_RESP=$(curl -s -X POST "$TICKET_SVC/orders" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "$ORDER_PAYLOAD")

ORDER_ID=$(echo "$ORDER_RESP" | jq -r .id)
TOTAL_AMOUNT=$(echo "$ORDER_RESP" | jq -r .totalAmount)

if [ -z "$ORDER_ID" ] || [ "$ORDER_ID" = "null" ]; then
  echo "Order creation failed: $ORDER_RESP" >&2
  exit 4
fi

echo "Order created: id=$ORDER_ID total=$TOTAL_AMOUNT"

# Create payment
echo "Creating payment..."
PAYLOAD=$(jq -n --arg oid "$ORDER_ID" --arg uid "21" --arg amt "$TOTAL_AMOUNT" '{orderId:(($oid|tonumber)), userId:(($uid|tonumber)), amount:($amt|tonumber), paymentMethod:"WALLET"}')
PAY_RESP=$(curl -s -w "\n%{http_code}" -X POST "$TICKET_SVC/payments" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "$PAYLOAD")

PAY_BODY=$(echo "$PAY_RESP" | sed -n '1,$p' | head -n -1)
PAY_CODE=$(echo "$PAY_RESP" | tail -n1)

if [ "$PAY_CODE" -lt 200 ] || [ "$PAY_CODE" -ge 300 ]; then
  echo "Payment failed (HTTP $PAY_CODE): $PAY_BODY" >&2
  exit 5
fi

echo "Payment created: $PAY_BODY"

# Poll notifications
echo "Polling user notifications..."
MAX=10
COUNT=0
SUCCESS=0
while [ "$COUNT" -lt "$MAX" ]; do
  NOTIFS=$(curl -s -H "Authorization: Bearer $TOKEN" "$USER_SVC/notifications/user/21")
  # Look for a notification with title "Order Paid"
  if echo "$NOTIFS" | jq -e '.[] | select(.title=="Order Paid")' >/dev/null 2>&1; then
    echo "Notification found for Order Paid"
    echo "$NOTIFS" | jq
    SUCCESS=1
    break
  fi
  COUNT=$((COUNT+1))
  sleep 1
done

if [ "$SUCCESS" -ne 1 ]; then
  echo "Notification not found after $MAX attempts. Latest notifications: $NOTIFS" >&2
  exit 6
fi

echo "E2E payment->notification test succeeded."
