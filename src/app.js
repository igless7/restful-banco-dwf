import express from 'express';
import dotenv from 'dotenv';
import morgan from 'morgan';
import bankingRouter from './routes/banking.routes.js';
import correlation from './middleware/correlation.js';

dotenv.config();

const app = express();
app.use(express.json());
app.use(correlation());       // X-Correlation-Id para trazabilidad
app.use(morgan('combined'));  // logs básicos

app.use('/api', bankingRouter);

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`REST Proxy Banco escuchando en http://localhost:${port}`);
});
