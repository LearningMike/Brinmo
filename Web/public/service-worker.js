const cacheName = 'cache-v1';
const precacheResources = [
  '/404.html',
  '/login.html',
  '/dashboard.html',
  '/business-transactions.html',
  '/business-orders.html',
  '/business-inventory.html',
  '/business-settings.html',
  '/assets/css/main.css',
  '/assets/css/font-awesome.min.css'
];

self.addEventListener('install', event => {
  console.log('Service worker install event!');
  event.waitUntil(
    caches.open(cacheName)
      .then(cache => {
        return cache.addAll(precacheResources);
      })
  );
});

self.addEventListener('activate', event => {
  console.log('Service worker activate event!');
});

self.addEventListener('fetch', event => {
  console.log('Fetch intercepted for:', event.request.url);
  event.respondWith(caches.match(event.request)
    .then(cachedResponse => {
        if (cachedResponse) {
          return cachedResponse;
        }
        return fetch(event.request);
      })
    );
});