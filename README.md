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

## Publishing backend Docker images

The GitHub Actions workflow in `.github/workflows/docker-publish.yml` builds one
image for each backend service using `backend/Dockerfile`. Add these repository
secrets and variables under **Settings → Secrets and variables → Actions**:

- `DOCKERHUB_USERNAME`: your Docker Hub username.
- `DOCKERHUB_TOKEN`: a Docker Hub access token with permission to push images.
- `SONAR_TOKEN` (secret): a SonarQube token with permission to analyze the project.
- `SONAR_HOST_URL` (variable): the URL of your SonarQube server, reachable from GitHub-hosted runners.
- `SONAR_PROJECT_KEY` (variable): the project key configured in SonarQube.

Pushes to `main`, version tags matching `v*`, and manually started workflows
publish images to the single Docker Hub repository `ecom` under your username.
Each service has its own tags to keep the images separate, for example:
`<username>/ecom:product-service-latest` and
`<username>/ecom:product-service-sha-<commit>`. Version releases use tags such
as `<username>/ecom:product-service-v1.0.0`. Pull requests targeting `main`
build and scan the images without publishing them. On publishing runs,
SonarQube analysis and its quality gate pass before image builds; Trivy then
scans each built image for HIGH and CRITICAL OS and library vulnerabilities.
Images are published only when both checks pass. Pull requests build and run
Trivy scans but skip SonarQube and do not publish. The storefront is not
included because it is currently built and served as static files rather than
as a Docker container.

## Known limits

- There is no sign-in yet; the storefront uses a single demo customer.
- Payments are simulated and always approved.
- Payment and order saving are not wrapped in one transaction across services.
- HTTPS is not configured; add a certificate (for example with Certbot) before taking real customers.
