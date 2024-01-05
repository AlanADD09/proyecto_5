//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
const express = require('express');
const path = require('path');
const cors = require('cors');
const axios = require('axios');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.static(path.join(__dirname)));
app.use(cors());
app.use(express.json()); // Middleware para parsear el cuerpo de las solicitudes como JSON

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.post('/search', async (req, res) => {
  const { input } = req.body; // Recuperar el valor 'input' desde el cuerpo de la solicitud
  try {
    const requestData = {
      input: input
    };

    const response = await axios.post('http://localhost:8080/process', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    res.json(response.data); // Enviar la respuesta al cliente
  } catch (error) {
    console.error('Hubo un problema con la solicitud:', error);
    res.status(500).send('Hubo un problema con la solicitud');
  }
});

app.listen(PORT, () => {
  console.log(`Servidor corriendo en el puerto ${PORT}`);
});
