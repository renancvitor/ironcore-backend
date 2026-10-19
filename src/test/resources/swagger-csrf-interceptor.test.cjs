// Run after SwaggerCsrfIntegrationTest: node --test src/test/resources/swagger-csrf-interceptor.test.cjs
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { runInNewContext } = require('node:vm');

function interceptor(fetch) {
  let options;
  const bundle = config => { options = config; return {}; };
  bundle.presets = { apis: {} };
  bundle.plugins = { DownloadUrl: {} };
  const window = { location: { href: 'http://localhost:8080/swagger-ui/index.html' } };
  runInNewContext(readFileSync('target/swagger-csrf-initializer.js', 'utf8'), {
    window, SwaggerUIBundle: bundle, SwaggerUIStandalonePreset: {}, URL, fetch,
  });
  window.onload();
  assert.equal(typeof options.requestInterceptor, 'function');
  return options.requestInterceptor;
}

test('served Swagger JavaScript fetches a fresh masked token for login, writes, logout and relogin', async () => {
  let count = 0;
  const hook = interceptor(async (url, options) => {
    assert.equal(url.href, 'http://localhost:8080/api/auth/csrf');
    assert.equal(options.credentials, 'same-origin');
    assert.equal(options.cache, 'no-store');
    return { ok: true, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: `masked-${++count}` }) };
  });
  for (const [method, url] of [
    ['POST', '/api/auth/login'], ['PUT', '/api/users/me/change-nickname'],
    ['POST', '/api/auth/logout'], ['POST', '/api/auth/login'],
  ]) {
    const original = { method, url, headers: { 'Content-Type': 'application/json' } };
    const request = await hook(original);
    assert.equal(request, original);
    assert.equal(request.headers['X-XSRF-TOKEN'], `masked-${count}`);
    assert.equal(request.headers['Content-Type'], 'application/json');
    assert.equal(request.credentials, 'same-origin');
  }
  assert.equal(count, 4);
});

test('shares only in-flight bootstrap between concurrent writes', async () => {
  let count = 0;
  let finish;
  const hook = interceptor(() => {
    count++;
    return new Promise(resolve => { finish = resolve; });
  });
  const first = hook({ method: 'POST', url: '/api/one' });
  const second = hook({ method: 'DELETE', url: '/api/two' });
  assert.equal(count, 1);
  finish({ ok: true, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'masked' }) });
  for (const request of await Promise.all([first, second])) {
    assert.equal(request.headers['X-XSRF-TOKEN'], 'masked');
  }
});

test('does not obtain or leak tokens for safe methods, foreign origins or non-API paths', async () => {
  const hook = interceptor(() => { assert.fail('Unexpected token fetch'); });
  for (const request of [
    { method: 'GET', url: '/api/users/me' },
    { method: 'HEAD', url: '/api/users/me' },
    { method: 'OPTIONS', url: '/api/auth/login' },
    { method: 'POST', url: 'https://foreign.test/api/auth/login' },
    { method: 'POST', url: 'http://localhost.evil.test:8080/api/auth/login' },
    { method: 'POST', url: '/other' },
  ]) {
    assert.equal(await hook(request), request);
    assert.equal(request.headers, undefined);
  }
});

test('rejects the write when CSRF retrieval fails and allows a later retrieval', async () => {
  let count = 0;
  const hook = interceptor(async () => ++count === 1
    ? { ok: false }
    : { ok: true, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'renewed' }) });
  await assert.rejects(hook({ method: 'POST', url: '/api/auth/login' }), /Unable to obtain CSRF token/);
  const request = await hook({ method: 'POST', url: '/api/auth/login' });
  assert.equal(request.headers['X-XSRF-TOKEN'], 'renewed');
  assert.equal(count, 2);
});
