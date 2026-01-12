# Service Booking Platform - Backend API

A Spring Boot REST API for connecting customers with professional service providers across various categories.

## 🎯 Overview

This platform allows customers to discover and book services from professional providers (tutors, consultants, healthcare professionals, etc.), while providers can manage their services, availability, and subscriptions.

## ✨ Key Features

### For Customers
- 👤 Register and manage account
- 🔍 Browse services by category
- 📅 View provider availability and book time slots
- 💳 Secure payment processing
- ⭐ Rate and review completed services
- 💰 Request refunds for cancelled bookings
- 🔔 Receive notifications for bookings and updates

### For Service Providers
- 👨‍💼 Register as a service provider
- 📋 Create and manage multiple services
- ⏰ Set weekly availability schedule
- 📆 View and manage bookings
- 💵 Subscription-based access (monthly fee)
- ⭐ Receive customer reviews
- 🔔 Get notified of new bookings

### For Admins
- 🛡️ Approve/reject provider registrations
- 💸 Manage refund requests
- 👥 Oversee platform users and activity

## 🛠️ Tech Stack

- **Backend Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **Payment Gateway:** Stripe & PayPal as simulation 
- **Build Tool:** Maven

## 📊 Database
- Supports customers, providers, services, bookings, payments, reviews, and more
- See [database schema](./schema.sql)
