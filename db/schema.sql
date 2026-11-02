create table if not exists products (
  id bigserial primary key,
  name text not null,
  description text not null default '',
  price_cents integer not null check (price_cents >= 0),
  stock integer not null default 0 check (stock >= 0),
  image_url text
);

create table if not exists cart_items (
  customer_id text not null,
  product_id bigint not null references products (id),
  quantity integer not null check (quantity > 0),
  primary key (customer_id, product_id)
);

create table if not exists credit_accounts (
  customer_id text primary key,
  credit_limit_cents integer not null check (credit_limit_cents >= 0)
);

create table if not exists payments (
  id bigserial primary key,
  customer_id text not null,
  amount_cents integer not null check (amount_cents > 0),
  status text not null check (status in ('APPROVED', 'DECLINED')),
  created_at timestamptz not null default now()
);

create table if not exists orders (
  id bigserial primary key,
  customer_id text not null,
  total_cents bigint not null check (total_cents >= 0),
  status text not null check (status in ('PLACED')),
  created_at timestamptz not null default now()
);

create index if not exists orders_customer_idx on orders (customer_id, created_at desc);

create table if not exists order_items (
  order_id bigint not null references orders (id),
  product_id bigint not null references products (id),
  quantity integer not null check (quantity > 0),
  unit_price_cents integer not null check (unit_price_cents >= 0),
  primary key (order_id, product_id)
);

insert into products (name, description, price_cents, stock, image_url)
select * from (values
  ('Canvas Backpack', 'Water-resistant daily backpack with a padded laptop sleeve.', 5900, 40, 'https://images.pexels.com/photos/9630186/pexels-photo-9630186.jpeg'),
  ('Ceramic Pour-Over Set', 'Hand-glazed dripper and carafe for slow, clean coffee.', 3450, 25, 'https://images.pexels.com/photos/12019613/pexels-photo-12019613.jpeg'),
  ('Trail Running Shoes', 'Lightweight shoes with grippy outsoles for mixed terrain.', 12900, 18, 'https://images.pexels.com/photos/260044/pexels-photo-260044.jpeg'),
  ('Insulated Water Bottle', 'Keeps drinks cold for 24 hours or hot for 12.', 2800, 60, 'https://images.pexels.com/photos/12115334/pexels-photo-12115334.jpeg')
) as seed (name, description, price_cents, stock, image_url)
where not exists (select 1 from products);

insert into credit_accounts (customer_id, credit_limit_cents)
values ('demo-customer', 50000)
on conflict (customer_id) do nothing;
