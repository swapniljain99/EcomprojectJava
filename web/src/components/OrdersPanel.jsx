import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { formatPrice } from '../format.js';

export default function OrdersPanel({ customerId }) {
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    api
      .getOrders(customerId)
      .then((data) => active && setOrders(data))
      .catch((err) => active && setError(err.message));
    return () => {
      active = false;
    };
  }, [customerId]);

  if (error) return <p className="error-text">{error}</p>;
  if (!orders) return <p className="empty">Loading orders...</p>;
  if (orders.length === 0) return <p className="empty">You have no orders yet.</p>;

  return (
    <ul className="orders">
      {orders.map((order) => (
        <li className="order-row" key={order.id}>
          <div>
            <strong>Order #{order.id}</strong>
            <span>{new Date(order.createdAt).toLocaleString()}</span>
          </div>
          <span className="status">{order.status}</span>
          <strong>{formatPrice(order.totalCents)}</strong>
        </li>
      ))}
    </ul>
  );
}
