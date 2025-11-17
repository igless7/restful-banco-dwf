import { v4 as uuidv4 } from 'uuid';

export default function correlation() {
  return (req, res, next) => {
    const incoming = req.header('X-Correlation-Id');
    const cid = incoming && incoming.trim() ? incoming : uuidv4();
    req.correlationId = cid;
    res.setHeader('X-Correlation-Id', cid);
    next();
  };
}
