import fetch from 'node-fetch';

const timeoutMs = Number(process.env.REQUEST_TIMEOUT_MS || 8000);

export async function httpRequest(method, url, { headers = {}, body, correlationId } = {}) {
  const controller = new AbortController();
  const t = setTimeout(() => controller.abort(), timeoutMs);

  const opts = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-Correlation-Id': correlationId,
      ...headers
    },
    signal: controller.signal
  };

  if (body !== undefined) opts.body = JSON.stringify(body);

  try {
    const res = await fetch(url, opts);
    const text = await res.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }

    if (!res.ok) {
      const err = new Error(`External request failed: ${res.status}`);
      err.status = res.status;
      err.payload = data;
      throw err;
    }
    return data;
  } finally {
    clearTimeout(t);
  }
}
