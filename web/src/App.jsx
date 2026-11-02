import { useCallback, useEffect, useState } from 'react';
import { DEMO_CUSTOMER_ID, api } from './api.js';
import ProductGrid from './components/ProductGrid.jsx';
import CartPanel from './components/CartPanel.jsx';
import OrdersPanel from './components/OrdersPanel.jsx';

const MAX_QUANTITY = 99;

export default function App() {
  const [view, setView] = useState('shop');
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [loadError, setLoadError] = useState('');
  const [notice, setNotice] = useState(null);
  const [placing, setPlacing] = useState(false);

  useEffect(() => {
    Promise.all([api.getProducts(), api.getCart(DEMO_CUSTOMER_ID)])
      .then(([items, lines]) => {
        setProducts(items);
        setCart(lines);
      })
      .catch((error) => setLoadError(error.message));
  }, []);

  useEffect(() => {
    if (!notice) return undefined;
    const timer = setTimeout(() => setNotice(null), 4000);
    return () => clearTimeout(timer);
  }, [notice]);

  const quantityFor = useCallback(
    (productId) => cart.find((line) => line.productId === productId)?.quantity ?? 0,
    [cart],
  );

  const cartCount = cart.reduce((sum, line) => sum + line.quantity, 0);

  async function setQuantity(productId, quantity) {
    try {
      setCart(await api.setCartQuantity(DEMO_CUSTOMER_ID, productId, quantity));
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  async function checkout() {
    setPlacing(true);
    try {
      const order = await api.placeOrder(DEMO_CUSTOMER_ID);
      setCart([]);
      setNotice({ type: 'success', text: `Order #${order.orderId} placed. Thank you!` });
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    } finally {
      setPlacing(false);
    }
  }

  return (
    <div className="app">
      <header className="site-header">
        <div className="brand">Harbor &amp; Co.</div>
        <nav className="site-nav">
          <button className={view === 'shop' ? 'active' : ''} onClick={() => setView('shop')}>
            Shop
          </button>
          <button className={view === 'cart' ? 'active' : ''} onClick={() => setView('cart')}>
            Cart <span className="badge">{cartCount}</span>
          </button>
          <button className={view === 'orders' ? 'active' : ''} onClick={() => setView('orders')}>
            Orders
          </button>
        </nav>
      </header>

      {notice && (
        <div className={`notice notice-${notice.type}`} role="status">
          {notice.text}
        </div>
      )}

      <main className="content">
        {loadError && <p className="error-text">Could not load the shop: {loadError}</p>}

        {!loadError && view === 'shop' && (
          <>
            <h1>Everyday goods, delivered</h1>
            <ProductGrid
              products={products}
              quantityFor={quantityFor}
              maxQuantity={MAX_QUANTITY}
              onAdd={(productId) => setQuantity(productId, quantityFor(productId) + 1)}
            />
          </>
        )}

        {!loadError && view === 'cart' && (
          <>
            <h1>Your cart</h1>
            <CartPanel
              products={products}
              cart={cart}
              maxQuantity={MAX_QUANTITY}
              placing={placing}
              onChange={setQuantity}
              onCheckout={checkout}
            />
          </>
        )}

        {!loadError && view === 'orders' && (
          <>
            <h1>Your orders</h1>
            <OrdersPanel customerId={DEMO_CUSTOMER_ID} />
          </>
        )}
      </main>
    </div>
  );
}
