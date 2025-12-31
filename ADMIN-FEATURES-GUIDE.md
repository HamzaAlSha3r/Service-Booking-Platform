# 👑 Admin Features - Phase 2

## ✅ What Has Been Implemented

### **1. Repositories**
- ✅ `RefundRepository` - Access refund data
- ✅ `BookingRepository` - Access booking data
- ✅ `ServiceRepository` - Access service data
- ✅ `SubscriptionRepository` - Access subscription data
- ✅ `CategoryRepository` - Access category data
- ✅ `TransactionRepository` - Access transaction data
- ✅ `UserRepository.findByAccountStatus()` - Get pending providers

---

### **2. Request DTOs**
- ✅ `ProviderApprovalRequest` - Admin notes for provider approval/rejection
- ✅ `RefundDecisionRequest` - Admin notes for refund approval/rejection

---

### **3. Response DTOs**
- ✅ `PendingProviderResponse` - Pending provider details
- ✅ `PendingRefundResponse` - Pending refund details
- ✅ `AdminStatsResponse` - Platform statistics

---

### **4. Service Layer**
- ✅ `AdminService` with methods:
  - `getPendingProviders()` - Get all pending service providers
  - `approveProvider(id, request)` - Approve provider registration
  - `rejectProvider(id, request)` - Reject provider registration
  - `getPendingRefunds()` - Get all pending refund requests
  - `approveRefund(id, request)` - Approve refund request
  - `rejectRefund(id, request)` - Reject refund request
  - `getPlatformStats()` - Get platform statistics

---

### **5. Controller Layer**
- ✅ `AdminController` with endpoints:
  - `GET /api/admin/providers/pending` - List pending providers
  - `PUT /api/admin/providers/{id}/approve` - Approve provider
  - `PUT /api/admin/providers/{id}/reject` - Reject provider
  - `GET /api/admin/refunds/pending` - List pending refunds
  - `PUT /api/admin/refunds/{id}/approve` - Approve refund
  - `PUT /api/admin/refunds/{id}/reject` - Reject refund
  - `GET /api/admin/stats` - Get platform statistics

---

### **6. Security**
- ✅ All endpoints protected with `@PreAuthorize("hasRole('ADMIN')")`
- ✅ Only users with ADMIN role can access

---

### **7. Testing**
- ✅ HTTP requests added to `request.http`
- ✅ Includes success and error test cases

---

## 🔐 Security Implementation

### **Authorization Check:**
```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    // All methods require ADMIN role
}
```

### **JWT Token Required:**
All requests must include:
```
Authorization: Bearer {ADMIN_TOKEN}
```

---

## 📝 API Endpoints

### **Provider Management**

#### 1. Get Pending Providers
```http
GET /api/admin/providers/pending
Authorization: Bearer {ADMIN_TOKEN}
```

**Response:**
```json
[
  {
    "id": 2,
    "firstName": "Ahmad",
    "lastName": "Ali",
    "email": "ahmad@example.com",
    "phone": "+970599654321",
    "bio": "Experienced Java Developer with 5+ years",
    "professionalTitle": "Senior Java Developer",
    "certificateUrl": "https://example.com/certificates/ahmad-java.pdf",
    "accountStatus": "PENDING_APPROVAL",
    "roles": ["SERVICE_PROVIDER"],
    "createdAt": "2025-12-31T10:30:00"
  }
]
```

---

#### 2. Approve Provider
```http
PUT /api/admin/providers/{id}/approve
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "adminNotes": "Certificate verified and approved"
}
```

**Response:**
```json
"Provider approved successfully"
```

**What Happens:**
1. ✅ User `account_status` changed to `ACTIVE`
2. ✅ Provider can now create services
3. ✅ Provider receives `ACCOUNT_APPROVED` notification (TODO)

---

#### 3. Reject Provider
```http
PUT /api/admin/providers/{id}/reject
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "adminNotes": "Certificate not valid or incomplete information"
}
```

**Response:**
```json
"Provider rejected successfully"
```

**What Happens:**
1. ✅ User `account_status` changed to `REJECTED`
2. ✅ Provider cannot create services
3. ✅ Provider receives `ACCOUNT_REJECTED` notification (TODO)

---

### **Refund Management**

#### 4. Get Pending Refunds
```http
GET /api/admin/refunds/pending
Authorization: Bearer {ADMIN_TOKEN}
```

**Response:**
```json
[
  {
    "id": 1,
    "bookingId": 5,
    "customerName": "Hamza Shaer",
    "customerEmail": "hamza@example.com",
    "serviceName": "Java 8 Advanced Course",
    "providerName": "Ahmad Ali",
    "refundAmount": 50.00,
    "refundReason": "Service cancelled by provider",
    "status": "PENDING",
    "requestedAt": "2025-12-31T14:00:00",
    "bookingDate": "2025-12-25T10:00:00"
  }
]
```

---

#### 5. Approve Refund
```http
PUT /api/admin/refunds/{id}/approve
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "adminNotes": "Refund approved due to service cancellation"
}
```

**Response:**
```json
"Refund approved successfully"
```

**What Happens:**
1. ✅ Refund `status` changed to `APPROVED`
2. ✅ Refund processed via Stripe (TODO)
3. ✅ Transaction status updated to `REFUNDED` (TODO)
4. ✅ Customer receives `REFUND_APPROVED` notification (TODO)

---

#### 6. Reject Refund
```http
PUT /api/admin/refunds/{id}/reject
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "adminNotes": "Refund rejected - service was completed as scheduled"
}
```

**Response:**
```json
"Refund rejected successfully"
```

**What Happens:**
1. ✅ Refund `status` changed to `REJECTED`
2. ✅ Customer receives `REFUND_REJECTED` notification (TODO)

---

### **Platform Statistics**

#### 7. Get Platform Stats
```http
GET /api/admin/stats
Authorization: Bearer {ADMIN_TOKEN}
```

**Response:**
```json
{
  "totalUsers": 150,
  "totalCustomers": 120,
  "totalProviders": 25,
  "activeProviders": 20,
  "pendingProviders": 3,
  "rejectedProviders": 2,
  "totalBookings": 500,
  "pendingBookings": 10,
  "confirmedBookings": 400,
  "completedBookings": 85,
  "cancelledBookings": 5,
  "totalRevenue": 25000.00,
  "pendingPayments": 500.00,
  "completedPayments": 24500.00,
  "totalRefunds": 10,
  "pendingRefunds": 2,
  "approvedRefunds": 7,
  "rejectedRefunds": 1,
  "totalRefundAmount": 1500.00,
  "totalServices": 80,
  "activeServices": 75,
  "totalCategories": 10,
  "activeSubscriptions": 20,
  "expiredSubscriptions": 5
}
```

**Note:** Currently returns basic statistics. Full implementation is TODO.

---

## 🧪 Testing Guide

### **Prerequisites:**
1. ✅ Application running
2. ✅ Admin user exists (created automatically)
3. ✅ At least one service provider registered

---

### **Test Scenario 1: Provider Approval**

**Step 1:** Register a new service provider (Request #2)
```http
POST /api/auth/register
{
  "firstName": "Ahmad",
  "lastName": "Ali",
  "email": "ahmad@example.com",
  "password": "Ahmad@12345",
  "phone": "+970599654321",
  "role": "SERVICE_PROVIDER",
  "bio": "Experienced Java Developer",
  "professionalTitle": "Senior Java Developer",
  "certificateUrl": "https://example.com/cert.pdf"
}
```

**Step 2:** Login as Admin (Request #3)
```http
POST /api/auth/login
{
  "email": "admin@trainingproject.com",
  "password": "Admin@12345"
}
```
**Copy the `token` from response**

---

**Step 3:** Get pending providers (Request #11)
```http
GET /api/admin/providers/pending
Authorization: Bearer {PASTE_ADMIN_TOKEN_HERE}
```

Expected: List containing Ahmad's registration

---

**Step 4:** Approve provider (Request #12)
```http
PUT /api/admin/providers/2/approve
Authorization: Bearer {PASTE_ADMIN_TOKEN_HERE}
{
  "adminNotes": "Approved"
}
```

Expected: `"Provider approved successfully"`

---

**Step 5:** Verify provider is approved
```http
GET /api/admin/providers/pending
```

Expected: Empty list (Ahmad is now ACTIVE)

---

### **Test Scenario 2: Error Cases**

#### Access without token (Request #18):
```http
GET /api/admin/providers/pending
```
**Expected:** `401 Unauthorized`

---

#### Access as Customer (Request #19):
1. Login as customer (Request #4)
2. Use customer token in admin endpoint

**Expected:** `403 Forbidden`

---

#### Approve non-existent provider (Request #20):
```http
PUT /api/admin/providers/99999/approve
```
**Expected:** `404 Not Found - Provider not found with ID: 99999`

---

## 🔄 Business Logic

### **Provider Approval Flow:**
```
1. Service Provider registers → account_status = PENDING_APPROVAL
   ↓
2. Admin views pending providers
   ↓
3. Admin reviews certificate & info
   ↓
4a. Admin approves → account_status = ACTIVE
    → Provider can create services
    → Notification sent
   ↓
4b. Admin rejects → account_status = REJECTED
    → Provider cannot create services
    → Notification sent with reason
```

---

### **Refund Approval Flow:**
```
1. Customer cancels booking
   ↓
2. System calculates refund amount:
   - <24hrs before booking: Auto-approve 100%
   - >24hrs before booking: Create pending refund (50%)
   ↓
3. Admin reviews refund request
   ↓
4a. Admin approves:
    → Refund processed via Stripe
    → Transaction marked as REFUNDED
    → Time slot becomes AVAILABLE
    → Notification sent to customer
   ↓
4b. Admin rejects:
    → No refund processed
    → Notification sent to customer with reason
```

---

## 📊 Database Changes

### **User Table:**
- `account_status` changes:
  - `PENDING_APPROVAL` → `ACTIVE` (on approval)
  - `PENDING_APPROVAL` → `REJECTED` (on rejection)

### **Refund Table:**
- `status` changes:
  - `PENDING` → `APPROVED` (on approval)
  - `PENDING` → `REJECTED` (on rejection)
- `admin_notes` field populated with decision reason
- `processed_at` timestamp set

---

## ⚠️ TODO Items

These features are marked as TODO in the code:

1. **Notifications:**
   - Send `ACCOUNT_APPROVED` notification to provider
   - Send `ACCOUNT_REJECTED` notification to provider
   - Send `REFUND_APPROVED` notification to customer
   - Send `REFUND_REJECTED` notification to customer

2. **Payment Processing:**
   - Process actual refund via Stripe
   - Update transaction status to REFUNDED

3. **Statistics:**
   - Implement full statistics calculation
   - Add caching for performance

4. **Time Slot Management:**
   - Update time_slot status back to AVAILABLE on refund approval

---

## 🎯 Next Steps

After testing Phase 2, we'll move to:
- **Phase 3:** Customer Features (browse services, book appointments, reviews)
- **Phase 4:** Provider Features (manage services, availability, subscriptions)

---

**Last Updated:** December 31, 2025

