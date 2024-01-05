//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
// Obtener el nombre del libro seleccionado del localStorage
const selectedBook = localStorage.getItem('selectedBook');

if (selectedBook) {
// Construir la ruta del archivo basado en el nombre del libro
const filePath = `LIBROS_TXT/${selectedBook}`;

// Cargar el contenido del archivo
fetch(filePath)
    .then(response => {
      if (!response.ok) {
        throw new Error('El archivo no pudo ser encontrado');
      }
      return response.text();
    })
    .then(text => {
      mostrarContenido(text);
    })
    .catch(error => {
      console.error('Error:', error);
    });
} else {
  console.error('No se seleccionó ningún libro.');
}

function mostrarContenido(contenido) {
  let contenidoArchivo = document.getElementById('contenidoArchivo');
  contenidoArchivo.innerText = contenido;
}