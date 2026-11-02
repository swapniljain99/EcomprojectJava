import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/products': { target: 'http://localhost:8081' },
      '/api/cart': { target: 'http://localhost:8082' },
      '/api/orders': { target: 'http://localhost:8085' },
    },
  },
});
