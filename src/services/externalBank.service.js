import { httpRequest } from '../utils/http.js';

const BASE = process.env.EXTERNAL_BASE_URL;

// Mapea endpoints locales a externos.
// Ajusta las rutas externas según el sistema que consumas.
export async function obtenerCuentasPorDUI(dui, correlationId) {
  const url = `${BASE}/cuentas/${encodeURIComponent(dui)}`;
  return httpRequest('GET', url, { correlationId });
}

export async function abonarEfectivo(numeroCuenta, monto, correlationId) {
  const url = `${BASE}/abonarefectivo`;
  return httpRequest('POST', url, { correlationId, body: { numeroCuenta, monto } });
}

export async function retirarEfectivo(numeroCuenta, monto, correlationId) {
  const url = `${BASE}/retirarefectivo`;
  return httpRequest('POST', url, { correlationId, body: { numeroCuenta, monto } });
}
