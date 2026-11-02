import { formatPrice } from '../format.js';

export default function ProductGrid({ products, quantityFor, maxQuantity, onAdd }) {
  if (products.length === 0) {
    return <p className="empty">No products are available right now.</p>;
  }

  return (
    <div className="product-grid">
      {products.map((product) => {
        const inCart = quantityFor(product.id);
        return (
          <article className="product-card" key={product.id}>
            {product.imageUrl && <img src={product.imageUrl} alt={product.name} loading="lazy" />}
            <div className="product-body">
              <h2>{product.name}</h2>
              <p>{product.description}</p>
              <div className="product-footer">
                <span className="price">{formatPrice(product.priceCents)}</span>
                <button
                  className="primary"
                  onClick={() => onAdd(product.id)}
                  disabled={inCart >= maxQuantity}
                >
                  {inCart > 0 ? `Add another (${inCart} in cart)` : 'Add to cart'}
                </button>
              </div>
            </div>
          </article>
        );
      })}
    </div>
  );
}
