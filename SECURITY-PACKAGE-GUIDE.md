# Security Package - Complete Guide 🔐

## Overview

هاد الدليل بيشرح بالتفصيل الممل كل ملف في package `security` وكيف بيشتغلوا مع بعض.

---

## Package Structure 📁

```
com.testing.traningproject.security/
├── SecurityConfig.java
├── JwtService.java
├── JwtAuthenticationFilter.java
└── CustomUserDetailsService.java
```

**الملفات:**
- **SecurityConfig.java** - إعدادات الأمان الرئيسية
- **JwtService.java** - خدمة إنشاء والتحقق من JWT tokens
- **JwtAuthenticationFilter.java** - فلتر التحقق من التوكن في كل request
- **CustomUserDetailsService.java** - خدمة جلب المستخدم من Database

---

## 1. SecurityConfig.java 🔧

### Purpose
المدير العام لكل إعدادات الأمان في التطبيق.

### Responsibilities
- تحديد مين يقدر يدخل على أي endpoint
- إعداد JWT authentication
- ربط كل مكونات الأمان مع بعض

---

### Configuration Details

#### 1️⃣ Security Filter Chain

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
```

**الغرض:**
سلسلة من الفلاتر بتمر عليها كل HTTP request قبل ما توصل للـ Controller.

**الكود:**

```java
http
    .csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/provider/**").hasRole("SERVICE_PROVIDER")
        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
        .anyRequest().authenticated()
    )
    .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    .authenticationProvider(authenticationProvider())
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

**الشرح بالتفصيل:**

##### A. Disable CSRF

```java
.csrf(AbstractHttpConfigurer::disable)
```

**شو هو CSRF؟**
- Cross-Site Request Forgery = هجوم بيخلي المستخدم يعمل action بدون ما يدري
- مثال: أنت فاتح Facebook، موقع تاني بيبعت request باسمك

**ليش عطلناه؟**
- احنا بنستخدم JWT tokens (stateless)
- CSRF بيشتغل مع sessions (stateful)
- لما تستخدم JWT، CSRF مش مشكلة

**ملاحظة:** لو كنا بنستخدم sessions + cookies، لازم نفعل CSRF protection

---

##### B. Authorization Rules

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/provider/**").hasRole("SERVICE_PROVIDER")
    .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
    .anyRequest().authenticated()
)
```

**الشرح:**

| Pattern | Rule | معناه |
|---------|------|-------|
| `/api/auth/**` | `permitAll()` | مفتوح للكل (register, login) |
| `/api/admin/**` | `hasRole("ADMIN")` | للأدمن فقط |
| `/api/provider/**` | `hasRole("SERVICE_PROVIDER")` | للبروفايدر فقط |
| `/api/customer/**` | `hasRole("CUSTOMER")` | للكاستومر فقط |
| `anyRequest()` | `authenticated()` | أي طلب تاني لازم تكون مسجل دخول |

**ملاحظة مهمة:**
- Spring Security بيضيف prefix `ROLE_` تلقائياً
- لما تكتب `hasRole("ADMIN")` → Spring بدور على `ROLE_ADMIN`

---

##### C. Session Management

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**شو يعني STATELESS؟**
- السيرفر **مش بيحفظ** أي معلومات عن المستخدم
- كل request بيجي مع JWT token فيه كل المعلومات
- مش بنستخدم server-side sessions

**الفرق بين Stateful و Stateless:**

| Feature | Stateful (Session) | Stateless (JWT) |
|---------|-------------------|-----------------|
| Storage | Server stores session | Server stores nothing |
| Scalability | Hard to scale | Easy to scale |
| Security | Session ID in cookie | JWT in header |
| Logout | Delete session | Client deletes token |

---

##### D. Authentication Provider

```java
.authenticationProvider(authenticationProvider())
```

**شو يعني؟**
بنادي على method `authenticationProvider()` الي بترجع `DaoAuthenticationProvider`:

```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

**DaoAuthenticationProvider:**
- DAO = Data Access Object
- بيجيب المستخدم من Database عن طريق `UserDetailsService`
- بيقارن الـ password باستخدام `PasswordEncoder`

**الربط:**
```
authenticationProvider
    ├── Uses: CustomUserDetailsService (to load user from DB)
    └── Uses: BCryptPasswordEncoder (to verify password)
```

---

##### E. Add JWT Filter

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

**شو يعني؟**
- بنضيف `JwtAuthenticationFilter` **قبل** `UsernamePasswordAuthenticationFilter`

**ترتيب الفلاتر:**
```
HTTP Request
    ↓
JwtAuthenticationFilter (احنا عملناه)
    ↓
UsernamePasswordAuthenticationFilter (Spring Security)
    ↓
Controller
```

**ليش قبل؟**
- بدنا **أول شي** نتحقق من JWT token
- لو التوكن صح → نحط المستخدم في SecurityContext
- لو التوكن غلط → نرجع 401 Unauthorized

---

#### 2️⃣ Password Encoder Bean

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**شو يعني BCrypt؟**
- One-way hashing algorithm = تشفير باتجاه واحد
- مش ممكن ترجع من hash للـ plain text

**Strength 12:**
- 2^12 iterations = 4096 iteration
- كل ما الـ strength أعلى = أبطأ بس أأمن
- Recommended: 10-12 for production

**مثال:**
```java
String plainPassword = "password123";
String hashed = passwordEncoder.encode(plainPassword);
// Result: $2a$12$xyz123abc456... (60 characters)

boolean matches = passwordEncoder.matches("password123", hashed);
// Result: true
```

---

#### 3️⃣ Authentication Manager Bean

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    try {
        return config.getAuthenticationManager();
    } catch (Exception e) {
        throw new RuntimeException("Failed to get authentication manager", e);
    }
}
```

**ليش بحتاجه؟**
بنستخدمه في `AuthService` عشان نعمل authentication:

```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(email, password)
);
```

**شو بيعمل؟**
Spring Security بيوفرلك authentication manager جاهز من `AuthenticationConfiguration`.

---

---

## 2. JwtService.java 🎫

### Purpose
مصنع إنشاء والتحقق من JWT tokens.

### Responsibilities
- إنشاء access tokens (24 hours)
- إنشاء refresh tokens (7 days)
- التحقق من صحة التوكن
- استخراج المعلومات من التوكن (email, expiration, etc.)

---

### Configuration Properties

```java
@Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
private String secretKey;

@Value("${jwt.expiration:86400000}") // 24 hours
private Long jwtExpiration;

@Value("${jwt.refresh.expiration:604800000}") // 7 days
private Long refreshExpiration;
```

**شو يعني `@Value`؟**
- بيجيب القيمة من `application.properties`
- لو مش موجودة → بيستخدم الـ default value بعد الـ `:`

**القيم:**
- **86400000 ms** = 24 hours
- **604800000 ms** = 7 days
- **Secret Key** لازم يكون 256 bits على الأقل

---

### Key Methods

#### 1️⃣ Extract Username

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
```

**الغرض:** استخراج الـ email من التوكن.

**`Claims::getSubject`:** method reference بيرجع الـ subject (احنا حطينا email).

---

#### 2️⃣ Extract Claim (Generic)

```java
public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}
```

**شو يعني Generic `<T>`؟**
الـ method بترجع **أي نوع**:
- `extractClaim(token, Claims::getSubject)` → Returns String
- `extractClaim(token, Claims::getExpiration)` → Returns Date

---

#### 3️⃣ Generate Token

```java
public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
}

public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
}
```

**extraClaims:** بيانات إضافية في التوكن (مثل role, userId). احنا ما بنضيف شي (empty HashMap).

---

#### 4️⃣ Build Token

```java
private String buildToken(
    Map<String, Object> extraClaims,
    UserDetails userDetails,
    long expiration
) {
    return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey())
            .compact();
}
```

**مثال JWT Token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJoYW16YUBleGFtcGxlLmNvbSIsImlhdCI6MTYwOTQ1OTIwMCwiZXhwIjoxNjA5NTQ1NjAwfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**الأجزاء الثلاثة (مفصولة بـ `.`):**
1. **Header**: `{"alg":"HS256","typ":"JWT"}`
2. **Payload**: `{"sub":"hamza@example.com","iat":1609459200,"exp":1609545600}`
3. **Signature**: `SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`

---

#### 5️⃣ Validate Token

```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}
```

**شو بتحقق؟**
1. Username في التوكن = نفس username للمستخدم
2. التوكن مش منتهي

---

#### 6️⃣ Get Signing Key

```java
private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

**الغرض:**
- بيحول Base64 secret key لـ byte array
- بيعمل HMAC key للتوقيع

---

---

## 3. JwtAuthenticationFilter.java 🛡️

### Purpose
حارس البوابة - بيتحقق من JWT token في كل request.

### Responsibilities
- فحص Authorization header
- استخراج JWT token
- التحقق من صحة التوكن
- حط المستخدم في SecurityContext لو التوكن صح

---

### Extends OncePerRequestFilter

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter
```

**شو يعني OncePerRequestFilter؟**
- Filter من Spring بيضمن الفلتر ينفذ **مرة واحدة بس** لكل request
- حتى لو في internal forwards

---

### The Main Method

```java
protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
) throws ServletException, IOException
```

**الـ Parameters:**
- **HttpServletRequest**: الطلب الجاي من العميل
- **HttpServletResponse**: الرد الي بنرجعه
- **FilterChain**: سلسلة الفلاتر الباقية

---

### Filter Flow

#### Step 1: Extract JWT Token

```java
final String authHeader = request.getHeader("Authorization");

if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}

jwt = authHeader.substring(7); // Remove "Bearer " prefix
```

**شو بصير؟**
1. بيجيب Authorization header
2. لو مش موجود أو مش بيبدأ بـ "Bearer " → Skip
3. لو موجود → بياخد التوكن (بدون "Bearer ")

**مثال:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                      👆 من هون بنبدأ ناخد
```

---

#### Step 2: Extract Email

```java
userEmail = jwtService.extractUsername(jwt);
```

بيستخدم `JwtService` لاستخراج email من التوكن.

---

#### Step 3: Validate and Set Authentication

```java
if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
    
    if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
```

**شو بصير؟**
1. لو email موجود && المستخدم مش authenticated:
   - بيجيب المستخدم من Database
   - بيتحقق من التوكن
2. لو التوكن صح:
   - بيعمل authentication token
   - بيحطه في SecurityContext
3. لو التوكن غلط:
   - ما بيعمل شي → الـ endpoint بيرجع 401

---

#### Step 4: Continue Filter Chain

```java
filterChain.doFilter(request, response);
```

بيكمل على الفلتر التاني.

---

---

## 4. CustomUserDetailsService.java 👤

### Purpose
جالب المستخدم من Database.

### Responsibilities
- جلب المستخدم بناءً على email
- تحويل User entity لـ UserDetails (Spring Security format)

---

### The Main Method

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPasswordHash())
        .authorities(user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
            .collect(Collectors.toList()))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
}
```

**شو بصير؟**
1. بيجيب المستخدم من Database (`userRepository.findByEmail`)
2. لو مش موجود → رمي `UsernameNotFoundException`
3. بيعمل `UserDetails` object:
   - **username**: email
   - **password**: hashed password
   - **authorities**: `ROLE_CUSTOMER`, `ROLE_ADMIN`, etc.
   - **account flags**: كلهم false (account active)

**ملاحظة مهمة:**
- `org.springframework.security.core.userdetails.User` = class من Spring Security (مش entity تبعنا!)
- `SimpleGrantedAuthority` = representation للـ role

---

---

## Complete Authentication Flows 🔄

### Flow 1: Registration

```
POST /api/auth/register
    ↓
AuthController.register()
    ↓
AuthService.register()
    ↓
1. Check email exists → userRepository.existsByEmail()
2. Get role from database → roleRepository.findByName()
3. Set account status (PENDING_APPROVAL for providers, ACTIVE for customers)
4. Hash password → passwordEncoder.encode()
5. Save user → userRepository.save()
6. Generate JWT → jwtService.generateToken()
7. Return AuthResponse
```

---

### Flow 2: Login

```
POST /api/auth/login
    ↓
AuthController.login()
    ↓
AuthService.login()
    ↓
1. Authenticate → authenticationManager.authenticate()
    ↓ (internally)
    DaoAuthenticationProvider
        ↓
        CustomUserDetailsService.loadUserByUsername()
            ↓
            userRepository.findByEmail() → Database
            ↓
        Return UserDetails
        ↓
        passwordEncoder.matches(plain, hashed)
        ↓
        ✅ Success or ❌ Exception
    ↓
2. Load user → userRepository.findByEmail()
3. Check account status (ACTIVE, SUSPENDED, etc.)
4. Generate JWT → jwtService.generateToken()
5. Return AuthResponse
```

---

### Flow 3: Protected Endpoint Request

```
GET /api/users/me
Authorization: Bearer eyJhbGci...
    ↓
JwtAuthenticationFilter.doFilterInternal()
    ↓
1. Extract token from "Authorization" header
2. Extract email → jwtService.extractUsername()
3. Load user → userDetailsService.loadUserByUsername()
4. Validate token → jwtService.isTokenValid()
5. Set authentication in SecurityContext
    ↓
SecurityFilterChain checks authorization rules
    ↓
✅ Allowed → Controller method
❌ Forbidden → 403 response
```

---

---

## How Components Work Together 🔗

### Dependency Injection Chain

```
SecurityConfig
    ├── Creates: PasswordEncoder (BCrypt)
    ├── Creates: AuthenticationManager
    ├── Creates: AuthenticationProvider
    │   ├── Uses: CustomUserDetailsService
    │   └── Uses: PasswordEncoder
    └── Adds: JwtAuthenticationFilter to filter chain

AuthService
    ├── Uses: AuthenticationManager (for login)
    ├── Uses: PasswordEncoder (for registration)
    ├── Uses: JwtService (to generate tokens)
    ├── Uses: UserRepository (to save/find users)
    └── Uses: RoleRepository (to get roles)

JwtAuthenticationFilter
    ├── Uses: JwtService (to validate tokens)
    └── Uses: UserDetailsService (to load user)

CustomUserDetailsService
    └── Uses: UserRepository (to find user by email)
```

---

### Spring Autowiring Explanation

**السؤال:** كيف Spring Security بيوصل تلقائياً لـ `CustomUserDetailsService`؟

**الجواب:** عن طريق **Dependency Injection** و **Spring Beans**.

#### Step 1: Create Bean

```java
@Service  // 👈 هاد الـ annotation
public class CustomUserDetailsService implements UserDetailsService {
    // ...
}
```

**شو بيعمل `@Service`؟**
- بيحكي لـ Spring: "هاد class هو Bean"
- Spring بيعمل Object واحد منه ويحفظه في Application Context

---

#### Step 2: Inject Bean

```java
@Configuration
@RequiredArgsConstructor  // 👈 هاد بيعمل constructor injection
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;  // 👈 Spring بيحقنه تلقائياً
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
```

**شو بصير؟**
1. Spring بيشوف `SecurityConfig` محتاج `UserDetailsService`
2. بيدور في Application Context على Bean من نوع `UserDetailsService`
3. بيلاقي `CustomUserDetailsService`
4. **بيحقنه تلقائياً** في الـ constructor

---

#### Step 3: Use in Authentication

```java
// في AuthService
authenticationManager.authenticate(...)
    ↓
// Spring Security تلقائياً:
DaoAuthenticationProvider → uses userDetailsService
    ↓
CustomUserDetailsService.loadUserByUsername(email)
    ↓
Database query
```

---

---

## Summary Tables 📊

### Classes Overview

| Class | Type | Purpose |
|-------|------|---------|
| **SecurityConfig** | Configuration | إعدادات الأمان الرئيسية |
| **JwtService** | Service | إنشاء والتحقق من JWT tokens |
| **JwtAuthenticationFilter** | Filter | فحص التوكن في كل request |
| **CustomUserDetailsService** | Service | جلب المستخدم من Database |

---

### Bean Definitions

| Bean | Where Defined | Purpose |
|------|---------------|---------|
| `SecurityFilterChain` | SecurityConfig | سلسلة فلاتر الأمان |
| `PasswordEncoder` | SecurityConfig | تشفير الـ passwords (BCrypt) |
| `AuthenticationProvider` | SecurityConfig | التحقق من الهوية |
| `AuthenticationManager` | SecurityConfig | إدارة عملية الـ authentication |
| `JwtService` | Auto (via @Component) | خدمة الـ JWT |
| `JwtAuthenticationFilter` | Auto (via @Component) | فلتر الـ JWT |
| `CustomUserDetailsService` | Auto (via @Service) | خدمة جلب المستخدم |

---

### Important Annotations

| Annotation | Purpose |
|------------|---------|
| `@Configuration` | بيحكي لـ Spring: هاد class فيه إعدادات |
| `@Service` | بيحكي لـ Spring: هاد service bean |
| `@Component` | بيحكي لـ Spring: هاد component bean |
| `@Bean` | بيحكي لـ Spring: هاد method بترجع bean |
| `@RequiredArgsConstructor` | بيعمل constructor لكل `final` fields |
| `@Value` | بيجيب قيمة من application.properties |

---

---

## Key Concepts 🎯

### 1. DaoAuthenticationProvider

**ما هو؟**
- Class من Spring Security للـ authentication عن طريق Database
- DAO = Data Access Object

**من وين؟**
```java
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
```

**شو بيعمل؟**
1. يجيب User من Database (via UserDetailsService)
2. يقارن passwords (via PasswordEncoder)
3. يرجع authentication success أو exception

**الاستخدام:**
```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

---

### 2. JWT Token Structure

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 . eyJzdWIiOiJoYW16YUBleGFtcGxlLmNvbSIsImlhdCI6MTYwOTQ1OTIwMCwiZXhwIjoxNjA5NTQ1NjAwfQ . SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
        Header                             Payload                                                                 Signature
```

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "hamza@example.com",
  "iat": 1609459200,
  "exp": 1609545600
}
```

**Signature:**
- HMAC SHA256 signature
- بيضمن التوكن ما اتغير

---

### 3. BCrypt Password Hashing

**مثال:**
```java
String plainPassword = "password123";
String hashed = passwordEncoder.encode(plainPassword);
// Result: $2a$12$N9qo8uLOickgx2ZMRZoMy.xyz123abc456...

// Verify password
boolean matches = passwordEncoder.matches("password123", hashed);
// Result: true

boolean wrongPassword = passwordEncoder.matches("wrongpass", hashed);
// Result: false
```

**الخصائص:**
- **One-way hashing**: مش ممكن ترجع من hash للـ plain text
- **Salt**: كل hash فيه salt عشوائي (نفس الـ password بيعطي hash مختلف كل مرة)
- **Strength 12**: 2^12 = 4096 iterations

---

### 4. Spring Security Context

**شو هو؟**
- مكان بيحفظ معلومات المستخدم المسجل حالياً
- موجود طول فترة الـ request

**الاستخدام:**
```java
// Set authentication
SecurityContextHolder.getContext().setAuthentication(authToken);

// Get current user
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getName();
```

---

---

## Security Best Practices 🚨

### ✅ What We Did Right

1. **BCrypt with Strength 12** - strong password hashing
2. **JWT Tokens** - stateless authentication
3. **HTTPS Only** - في production
4. **Role-Based Authorization** - حماية endpoints حسب الصلاحيات
5. **Account Status Checks** - منع المستخدمين المعلقين من الدخول
6. **Exception Handling** - عدم كشف معلومات حساسة في الأخطاء
7. **Input Validation** - في الـ DTOs

### ⚠️ Additional Recommendations

1. **Rate Limiting** - منع brute force attacks
2. **Token Blacklist** - للـ logout الفعلي
3. **Password Policy** - minimum length, complexity
4. **2FA** - Two-Factor Authentication
5. **Audit Logging** - تسجيل كل عمليات الدخول
6. **IP Whitelisting** - للـ admin endpoints

---

---

## Common Issues & Solutions 🐛

### Issue 1: 401 Unauthorized on Public Endpoints

**السبب:**
```java
// Wrong:
.requestMatchers("/api/auth/login").authenticated()
```

**الحل:**
```java
// Correct:
.requestMatchers("/api/auth/**").permitAll()
```

---

### Issue 2: JWT Token Not Working

**السبب المحتمل:**
- Authorization header missing
- Wrong format (should be "Bearer {token}")
- Token expired
- Secret key mismatch

**الحل:**
```javascript
// Frontend - correct format
headers: {
  'Authorization': 'Bearer ' + token
}
```

---

### Issue 3: hasRole() Not Working

**السبب:**
```java
// في CustomUserDetailsService - Wrong:
new SimpleGrantedAuthority(role.getName().name())

// Correct:
new SimpleGrantedAuthority("ROLE_" + role.getName().name())
```

Spring Security بيتوقع prefix `ROLE_`.

---

---

## References 📚

### Spring Security Documentation
- [Official Docs](https://docs.spring.io/spring-security/reference/index.html)
- [JWT with Spring Boot](https://www.baeldung.com/spring-security-jwt)

### Libraries Used
- `spring-boot-starter-security` - Spring Security
- `io.jsonwebtoken:jjwt-api` - JWT creation/parsing
- `io.jsonwebtoken:jjwt-impl` - JWT implementation
- `io.jsonwebtoken:jjwt-jackson` - JWT JSON processing

---

---

## Checklist ✅

عند إضافة feature جديد متعلق بالأمان:

- [ ] هل الـ endpoint محمي بالصلاحيات الصحيحة؟
- [ ] هل في input validation؟
- [ ] هل في exception handling مناسب؟
- [ ] هل البيانات الحساسة مش ظاهرة في الـ response؟
- [ ] هل الـ password متشفر قبل الحفظ؟
- [ ] هل الـ JWT token بينحذف عند الـ logout؟
- [ ] هل في logging للعمليات المهمة؟

---

**End of Security Package Guide** 🎉

