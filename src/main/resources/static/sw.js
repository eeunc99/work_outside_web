// ponytail: minimal offline shell cache. no versioned cache invalidation strategy;
// bump CACHE_NAME manually when static assets change meaningfully.
const CACHE_NAME = 'work-mgmt-shell-v1';
const SHELL_ASSETS = [
  '/css/main.css',
  '/css/login.css',
  '/image/icon-192.png',
  '/image/icon-512.png',
  '/manifest.json'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(SHELL_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.pathname.startsWith('/schedules/') || url.pathname.startsWith('/logins/') || url.pathname.startsWith('/api/')) {
    return;
  }

  event.respondWith(
    caches.match(request).then((cached) => cached || fetch(request))
  );
});
