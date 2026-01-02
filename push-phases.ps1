# PowerShell script to push Phase 3 & 4 to GitHub

Set-Location "C:\Users\hamza\Desktop\Traning QuizPlus\Traning-Project"

Write-Host "=== Current Branch ===" -ForegroundColor Cyan
git branch

Write-Host "`n=== Git Status ===" -ForegroundColor Cyan
git status

Write-Host "`n=== Adding all files ===" -ForegroundColor Cyan
git add .

Write-Host "`n=== Creating commit ===" -ForegroundColor Cyan
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

Write-Host "`n=== Pushing to GitHub ===" -ForegroundColor Cyan
git push origin main

Write-Host "`n=== Final Status ===" -ForegroundColor Green
git log --oneline -5

Write-Host "`nDone! Check GitHub to confirm." -ForegroundColor Green

