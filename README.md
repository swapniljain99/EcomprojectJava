# Harbor & Co. shop

A multi-service ecommerce app. Java services run as Docker containers on an Azure VM, the web storefront is served by nginx on the same VM, and all data lives in an AWS RDS PostgreSQL database.

## Services

| Service | Port | Purpose |
| --- | --- | --- |
| product-service | 8081 | Product catalog |
| cart-service | 8082 | Shopping cart per customer |
| credit-service | 8083 | Credit limit check |
| payment-service | 8084 | Payment recording (simulated approval) |
| order-service | 8085 | Places orders: cart, price, credit check, payment, save |

## Deploy steps

1. Run `db/schema.sql` once against the RDS database (any PostgreSQL client).
2. On the VM, copy `backend/.env.example` to `backend/.env` and fill in the RDS details.
3. In `backend/`, run `docker compose up -d --build`.
4. In `web/`, run `npm install` and `npm run build`, then copy `web/dist` to `/var/www/shop`.
5. Copy `deploy/shop-proxy.conf` to `/etc/nginx/` and `deploy/nginx.conf` to `/etc/nginx/sites-available/shop`, enable it, and reload nginx.
6. Open the VM's port 80 in the Azure network security group.

Health checks: `http://<vm>/api/products` should return the product list.

## Known limits

- There is no sign-in yet; the storefront uses a single demo customer.
- Payments are simulated and always approved.
- Payment and order saving are not wrapped in one transaction across services.
- HTTPS is not configured; add a certificate (for example with Certbot) before taking real customers.
