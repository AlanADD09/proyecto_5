//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
const searchContainer = document.querySelector('.search-container');

document.getElementById('searchForm').addEventListener('submit', async function(event) {
  event.preventDefault();
  const inputText = document.getElementById('searchInput').value;

  try {
    const response = await axios.post('/search', { input: inputText }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.data && response.data.length > 0) {
      const booksListContainer = document.getElementById('booksList');
      booksListContainer.innerHTML = '';

      // Mostrar los libros debajo de la barra de búsqueda
      response.data.forEach(book => {
        const formattedName = formatBookName(book);
      
        const listItem = document.createElement('li');
        listItem.textContent = formattedName;
      
        listItem.addEventListener('click', function() {
            // Almacenar el libro seleccionado en localStorage (opcional)
            localStorage.setItem('selectedBook', book);
      
            // Redirigir a la página book.html cuando se hace clic en un libro
            window.location.href = 'book.html';
        });
      
        booksListContainer.appendChild(listItem);
      });

      searchContainer.classList.add('books-displayed');

    } else {
      // Si no hay resultados, redirigir a una nueva página
      // window.location.href = 'pagina-sin-resultados.html';
    }
  } catch (error) {
    console.error('Hubo un problema con la solicitud:', error);
  }
});

function formatBookName(input) {
  var originalName = input 
  const regex = /^(.*),_(.*)__(.*)_\._(.*)_\[(.*)\]\.txt$/; // Expresión regular para capturar los dos strings
  const matches = originalName.match(regex);
  var value
  if (matches) {
    value = `${matches[4]}, ${matches[1]} ${matches[2]}, ${matches[3]}`;
  } else {
    console.log("No se encontró coincidencia");
    value = originalName
  }  
  return value
}