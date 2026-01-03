# 💳 **Test Payment Cards - للاختبار**

## ✅ **بطاقات صالحة (ستنجح عملية الدفع):**

### 1️⃣ **Visa Card**
```json
{
  "cardNumber": "4532015112830366",
  "cardHolderName": "HAMZA SHAER",
  "expiryMonth": 12,
  "expiryYear": 2028,
  "cvv": "123",
  "billingAddress": "123 Main Street, Ramallah, Palestine"
}
```

### 2️⃣ **MasterCard**
```json
{
  "cardNumber": "5425233430109903",
  "cardHolderName": "ALI AHMAD",
  "expiryMonth": 6,
  "expiryYear": 2027,
  "cvv": "456",
  "billingAddress": "456 Commerce Ave, Nablus, Palestine"
}
```

### 3️⃣ **American Express**
```json
{
  "cardNumber": "374245455400126",
  "cardHolderName": "JOHN SMITH",
  "expiryMonth": 3,
  "expiryYear": 2029,
  "cvv": "7890",
  "billingAddress": "789 Business Blvd, Jerusalem, Palestine"
}
```

---

## ❌ **بطاقات محظورة (ستفشل عملية الدفع):**

### 1️⃣ **All Zeros**
```json
{
  "cardNumber": "0000000000000000",
  "cardHolderName": "TEST USER",
  "expiryMonth": 12,
  "expiryYear": 2028,
  "cvv": "000",
  "billingAddress": "Test Address"
}
```
**النتيجة:** ❌ "This card has been declined. Please use a different card."

### 2️⃣ **All Ones**
```json
{
  "cardNumber": "1111111111111111",
  "cardHolderName": "TEST USER",
  "expiryMonth": 12,
  "expiryYear": 2028,
  "cvv": "111",
  "billingAddress": "Test Address"
}
```
**النتيجة:** ❌ "This card has been declined. Please use a different card."

---

## 🔒 **آلية التحقق:**

### **1. Luhn Algorithm (Mod 10)**
- يتم التحقق من صحة رقم البطاقة باستخدام خوارزمية Luhn
- هذا هو المعيار الصناعي لجميع شركات البطاقات الائتمانية

### **2. Expiry Date Validation**
- يجب أن تكون البطاقة سارية المفعول (لم تنتهِ صلاحيتها)
- `expiryYear` يجب أن يكون >= 2026
- `expiryMonth` يجب أن يكون بين 1-12

### **3. CVV Validation**
- Visa/MasterCard: 3 أرقام
- American Express: 4 أرقام

### **4. Security Features**
- ✅ رقم البطاقة **لا يُخزن أبدًا** في قاعدة البيانات
- ✅ يتم حفظ فقط آخر 4 أرقام مع `****` للباقي
- ✅ Transaction ID يُرجع من Payment Gateway

---

## 🧪 **كيفية توليد بطاقات اختبار صالحة:**

يمكنك استخدام أدوات online مثل:
- https://www.freeformatter.com/credit-card-number-generator-validator.html
- https://dnschecker.org/credit-card-generator.php

**ملاحظة:** تأكد من أن الأرقام تمر من **Luhn Algorithm** check!

---

## 📝 **مثال على Request كامل:**

### **Customer - Create Booking:**
```json
{
  "serviceId": 1,
  "slotId": 5,
  "paymentMethod": "Credit Card",
  "paymentCard": {
    "cardNumber": "4532015112830366",
    "cardHolderName": "HAMZA SHAER",
    "expiryMonth": 12,
    "expiryYear": 2028,
    "cvv": "123",
    "billingAddress": "123 Main Street, Ramallah, Palestine"
  }
}
```

### **Provider - Subscribe to Plan:**
```json
{
  "planId": 1,
  "paymentMethod": "Credit Card",
  "autoRenew": true,
  "paymentCard": {
    "cardNumber": "5425233430109903",
    "cardHolderName": "ALI AHMAD",
    "expiryMonth": 6,
    "expiryYear": 2027,
    "cvv": "456",
    "billingAddress": "456 Commerce Ave, Nablus, Palestine"
  }
}
```

---

## 🎯 **Payment Flow:**

```
1. Customer/Provider sends payment card info
2. PaymentService validates:
   ✓ Card number (Luhn Algorithm)
   ✓ Expiry date (not expired)
   ✓ CVV format
   ✓ Not in blocklist
3. If valid → Generate Transaction ID
4. If invalid → Throw BadRequestException
5. Save transaction with masked card number
6. Send notification to user
```

---

## ⚠️ **تحذير أمني:**

في **Production**، يجب:
- استخدام Stripe/PayPal API حقيقي
- تشفير البيانات بـ HTTPS/TLS
- عدم إرسال بيانات البطاقة عبر GET requests
- استخدام PCI-DSS compliant infrastructure
- Tokenize card numbers بدلاً من تخزينها

**هذا النظام الحالي للتوضيح والاختبار فقط!** 🔒

