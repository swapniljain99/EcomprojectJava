import { formatPrice } from '../format.js';

export default function CartPanel({ products, cart, maxQuantity, placing, onChange, onCheckout }) {
  const productById = new Map(products.map((product) => [product.id, product]));
  const lines = cart
    .map((line) => ({ ...line, product: productById.get(line.productId) }))
    .filter((line) => line.product);

  if (lines.length === 0) {
    return <p className="empty">Your cart is empty. Browse the shop to add items.</p>;
  }

  const total = lines.reduce((sum, line) => sum + line.product.priceCents * line.quantity, 0);

  return (
    <section className="cart">
      <ul className="cart-lines">
        {lines.map((line) => (
          <li className="cart-line" key={line.productId}>
            <div className="cart-line-info">
              <strong>{line.product.name}</strong>
              <span>{formatPrice(line.product.priceCents)} each</span>
            </div>
            <div className="quantity">
              <button
                aria-label={`Decrease ${line.product.name}`}
                onClick={() => onChange(line.productId, line.quantity - 1)}
              >
                -
              </button>
              <span>{line.quantity}</span>
              <button
                aria-label={`Increase ${line.product.name}`}
                onClick={() => onChange(line.productId, line.quantity + 1)}
                disabled={line.quantity >= maxQuantity}
              >
                +
              </button>
            </div>
            <span className="line-total">{formatPrice(line.product.priceCents * line.quantity)}</span>
          </li>
        ))}
      </ul>
      <div className="cart-summary">
        <span>Total</span>
        <strong>{formatPrice(total)}</strong>
        <button className="primary" onClick={onCheckout} disabled={placing}>
          {placing ? 'Placing order...' : 'Place order'}
        </button>
      </div>
    </section>
  );
}
