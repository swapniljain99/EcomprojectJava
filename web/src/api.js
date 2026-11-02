export const DEMO_CUSTOMER_ID = 'demo-customer';

async function readErrorMessage(response) {
  const body = await response.json().catch(() => null);
  return body?.message || `Something went wrong (${response.status}). Please try again.`;
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

const customerPath = (customerId) => encodeURIComponent(customerId);

export const api = {
  getProducts: () => request('/api/products'),
  getCart: (customerId) => request(`/api/cart/${customerPath(customerId)}`),
  setCartQuantity: (customerId, productId, quantity) =>
    request(`/api/cart/${customerPath(customerId)}/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),
  placeOrder: (customerId) =>
    request('/api/orders', { method: 'POST', body: JSON.stringify({ customerId }) }),
  getOrders: (customerId) => request(`/api/orders?customerId=${customerPath(customerId)}`),
};
