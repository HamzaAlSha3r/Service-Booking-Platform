#!/bin/bash
cd "C:\Users\hamza\Desktop\Traning QuizPlus\Traning-Project"

echo "=== Current Branch ==="
git branch

echo ""
echo "=== Git Status ==="
git status

echo ""
echo "=== Adding all files ==="
git add .

echo ""
echo "=== Creating commit ==="
git commit -m "Phase 3 & 4: Complete Service Provider and Customer Features

- Phase 3: Service Provider
  - ProviderService: Create/Update/Delete services
  - SubscriptionService: Subscribe to plans
  - ProviderAvailability: Set weekly schedules
  - SUBSCRIPTION_PAYMENT transaction

- Phase 4: Customer Features
  - BookingService: Create/Cancel bookings with BOOKING_PAYMENT & REFUND transactions
  - ReviewService: Submit reviews for completed bookings
  - TransactionService: View transaction history
  - PAYOUT transaction for providers
  - Auto-refund logic (100% < 24hrs, 50% > 24hrs)

- Updated AdminService: Process REFUND transactions
- Added 38 test requests in request.http
- All 4 transaction types working (SUBSCRIPTION_PAYMENT, BOOKING_PAYMENT, REFUND, PAYOUT)
"

echo ""
echo "=== Pushing to GitHub ==="
git push origin main

echo ""
echo "=== Final Status ==="
git log --oneline -5

