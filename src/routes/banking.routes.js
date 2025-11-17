import { Router } from 'express';
import { validate, getCuentasSchema, abonarSchema, retirarSchema } from '../middleware/validate.js';
import { obtenerCuentasPorDUI, abonarEfectivo, retirarEfectivo } from '../services/externalBank.service.js';

const router = Router();

// 1. GET: /api/cuentas/:DUI
router.get('/cuentas/:DUI', validate(getCuentasSchema), async (req, res) => {
  try {
    const dui = req.params.DUI;
    const data = await obtenerCuentasPorDUI(dui, req.correlationId);
    return res.json({ cuentas: data });
  } catch (err) {
    return res.status(err.status || 502).json({
      error: 'EXTERNAL_ERROR',
      message: err.message,
      external: err.payload ?? null
    });
  }
});

// 2. POST: /api/abonarefectivo
router.post('/abonarefectivo', validate(abonarSchema), async (req, res) => {
  try {
    const { numeroCuenta, monto } = req.body;
    const data = await abonarEfectivo(numeroCuenta, monto, req.correlationId);
    return res.status(201).json({ resultado: data });
  } catch (err) {
    return res.status(err.status || 502).json({
      error: 'EXTERNAL_ERROR',
      message: err.message,
      external: err.payload ?? null
    });
  }
});

// 3. POST: /api/retirarefectivo
router.post('/retirarefectivo', validate(retirarSchema), async (req, res) => {
  try {
    const { numeroCuenta, monto } = req.body;
    const data = await retirarEfectivo(numeroCuenta, monto, req.correlationId);
    return res.status(201).json({ resultado: data });
  } catch (err) {
    return res.status(err.status || 502).json({
      error: 'EXTERNAL_ERROR',
      message: err.message,
      external: err.payload ?? null
    });
  }
});

export default router;
