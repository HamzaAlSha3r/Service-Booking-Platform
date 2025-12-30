CREATE TABLE role (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL CHECK (name IN ('CUSTOMER', 'SERVICE_PROVIDER', 'ADMIN'))
);

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       phone VARCHAR(20),
                       password_hash VARCHAR(255) NOT NULL,
                       profile_picture_url VARCHAR(500),

    -- Service Provider specific fields
                       bio TEXT,
                       professional_title VARCHAR(200),
                       certificate_url VARCHAR(500),
                       account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'PENDING_APPROVAL', 'REJECTED')),

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- DENORMALIZED FIELDS (for performance - average_rating , total_reviews ,total_bookings)
);

-- One user can have multiple roles
CREATE TABLE user_role (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           role_id INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
                           assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE(user_id, role_id)
);

CREATE TABLE category (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(100) UNIQUE NOT NULL,
                          description TEXT,
                          icon_url VARCHAR(500),
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service (
                         id BIGSERIAL PRIMARY KEY,
                         provider_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
                         title VARCHAR(255) NOT NULL,
                         description TEXT NOT NULL,
                         price DECIMAL(10,2) NOT NULL CHECK (price > 0),
                         duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
                         service_type VARCHAR(20) NOT NULL CHECK (service_type IN ('ONLINE', 'IN_PERSON', 'BOTH')),
                         location_address TEXT,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- this table create because if I want add new subscription as in Chatgpt(plus,pro,business etc..)
CREATE TABLE subscription_plan (
                                   id SERIAL PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL,
                                   description TEXT,
                                   price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                                   duration_days INTEGER NOT NULL CHECK (duration_days > 0),
                                   is_active BOOLEAN DEFAULT TRUE,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription (
                              id BIGSERIAL PRIMARY KEY,
    -- RESTRICT  to Prevent accidental deletion of payment history
                              provider_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                              plan_id INTEGER NOT NULL REFERENCES subscription_plan(id) ON DELETE RESTRICT,
                              start_date DATE NOT NULL,
                              end_date DATE NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
                              auto_renew BOOLEAN DEFAULT TRUE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT check_dates CHECK (end_date > start_date)
);

-- provider_availability:   MONDAY 09:00-17:00
CREATE TABLE provider_availability (
                                       id BIGSERIAL PRIMARY KEY,
                                       provider_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
                                       start_time TIME NOT NULL,
                                       end_time TIME NOT NULL,
                                       is_active BOOLEAN DEFAULT TRUE,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       CONSTRAINT check_times CHECK (end_time > start_time),
                                       UNIQUE(provider_user_id, day_of_week, start_time, end_time)
);

-- for the service make more details when the time AVAILABLE
-- time_slot generation for Monday 2025-12-30:  09:00-10:00 AVAILABLE , 10:00-11:00 BOOKED etc ..
CREATE TABLE time_slot (
                           id BIGSERIAL PRIMARY KEY,
                           service_id BIGINT NOT NULL REFERENCES service(id) ON DELETE CASCADE,
                           slot_date DATE NOT NULL,
                           start_time TIME NOT NULL,
                           end_time TIME NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BOOKED', 'BLOCKED')),
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT check_slot_times CHECK (end_time > start_time),
                           UNIQUE(service_id, slot_date, start_time)
);

-- Booking process , when and what happen and how much and so on
-- hamza booking the service java 8 learning on 30 oct 12:00 PM , this PENDING to complete payment
CREATE TABLE booking (
                             id BIGSERIAL PRIMARY KEY,
                             customer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                             service_id BIGINT NOT NULL REFERENCES service(id) ON DELETE RESTRICT,
                             slot_id BIGINT UNIQUE NOT NULL REFERENCES time_slot(id) ON DELETE RESTRICT,
                             booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             total_price DECIMAL(10,2) NOT NULL CHECK (total_price > 0),
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
                             cancellation_reason TEXT,
                             cancelled_at TIMESTAMP,
                             completed_at TIMESTAMP,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- BOOKING_PAYMENT: Customer pays for a booking , booking_id
-- SUBSCRIPTION_PAYMENT: Provider pays for monthly subscription , subscription_id
-- REFUND: Money returned to customer (cancelled booking)
-- PAYOUT: Money transferred to provider (completed booking)
CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                             booking_id BIGINT REFERENCES booking(id) ON DELETE RESTRICT,
                             subscription_id BIGINT REFERENCES subscription(id) ON DELETE RESTRICT,
                             transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('BOOKING_PAYMENT', 'SUBSCRIPTION_PAYMENT', 'REFUND', 'PAYOUT')),
                             amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
                             payment_method VARCHAR(50),
    -- number from payment way useful in check and return for this transaction
    -- and for make connection between strip and my website
                             payment_gateway_transaction_id VARCHAR(255),
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
                             failure_reason TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT check_transaction_reference CHECK (
                                 (transaction_type = 'BOOKING_PAYMENT' AND booking_id IS NOT NULL) OR
                                 (transaction_type = 'SUBSCRIPTION_PAYMENT' AND subscription_id IS NOT NULL) OR
                                 (transaction_type IN ('REFUND', 'PAYOUT'))
                                 )
);

CREATE TABLE review (
                        id BIGSERIAL PRIMARY KEY,
                        booking_id BIGINT UNIQUE NOT NULL REFERENCES booking(id) ON DELETE RESTRICT,
                        rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                        comment TEXT,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refund (
                        id BIGSERIAL PRIMARY KEY,
                        booking_id BIGINT UNIQUE NOT NULL REFERENCES booking(id) ON DELETE RESTRICT,
                        transaction_id BIGINT REFERENCES transaction(id) ON DELETE RESTRICT,
                        refund_amount DECIMAL(10,2) NOT NULL CHECK (refund_amount > 0),
                        refund_reason TEXT NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')),
                        admin_notes TEXT,
                        requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        processed_at TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN (
                                                                                                  'BOOKING_CONFIRMED',
                                                                                                  'BOOKING_CANCELLED',
                                                                                                  'NEW_BOOKING_RECEIVED',
                                                                                                  'BOOKING_REMINDER',
                                                                                                  'REVIEW_RECEIVED',
                                                                                                  'REFUND_APPROVED',
                                                                                                  'REFUND_REJECTED',
                                                                                                  'SUBSCRIPTION_EXPIRING',
                                                                                                  'SUBSCRIPTION_EXPIRED',
                                                                                                  'ACCOUNT_APPROVED',
                                                                                                  'ACCOUNT_REJECTED',
                                                                                                  'PAYMENT_SUCCESS',
                                                                                                  'PAYMENT_FAILED'
                                  )),
                              title VARCHAR(255) NOT NULL,
                              message TEXT NOT NULL,
                              is_read BOOLEAN DEFAULT FALSE,
                              read_at TIMESTAMP,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);