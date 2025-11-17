import Joi from 'joi';

export const validate = (schema) => (req, res, next) => {
  const payload = { body: req.body, params: req.params, query: req.query };
  const { error, value } = schema.validate(payload, { abortEarly: false });
  if (error) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      details: error.details.map(d => ({ message: d.message, path: d.path })),
    });
  }
  req.validated = value;
  next();
};

// Schemas
export const getCuentasSchema = Joi.object({
  params: Joi.object({
    DUI: Joi.string().trim().pattern(/^\d{8}-\d$/).required() // formato típico DUI: 8 dígitos-1 dígito
  }).required()
});

export const abonarSchema = Joi.object({
  body: Joi.object({
    numeroCuenta: Joi.string().trim().required(),
    monto: Joi.number().positive().precision(2).required()
  }).required()
});

export const retirarSchema = Joi.object({
  body: Joi.object({
    numeroCuenta: Joi.string().trim().required(),
    monto: Joi.number().positive().precision(2).required()
  }).required()
});
