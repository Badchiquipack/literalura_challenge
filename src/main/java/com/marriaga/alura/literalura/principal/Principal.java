package com.marriaga.alura.literalura.principal;

import com.marriaga.alura.literalura.dto.AutorDTO;
import com.marriaga.alura.literalura.dto.LibroDTO;
import com.marriaga.alura.literalura.dto.RespuestaLibrosDTO;
import com.marriaga.alura.literalura.model.Autor;
import com.marriaga.alura.literalura.model.Libro;
import com.marriaga.alura.literalura.service.AutorService;
import com.marriaga.alura.literalura.service.ConsumoAPI;
import com.marriaga.alura.literalura.service.ConvierteDatos;
import com.marriaga.alura.literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {

    @Autowired
    private LibroService libroService;

    @Autowired
    private AutorService autorService;

    @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConvierteDatos convierteDatos;

    private static final String URL_BASE = "https://gutendex.com/books/";

    public void mostrarMenu() {
        Scanner entrada = new Scanner(System.in);

        System.out.println("""
                
                Bienvenido a la plataforma Literalura. 
                A continuación se muestra un menú con las opciones válidas. 
                """);

        int opcion = -1;
        while (opcion != 0) {
            String menu = """
                    ==============================================
                    Ingrese el número de la opción deseada:
                    
                    1 - Buscar libro por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en determinado año
                    5 - Listar libros por idioma
                    0 - Salir
                    
                    ==============================================
                    """;
            System.out.println(menu);
            try {
                opcion = entrada.nextInt();
                entrada.nextLine();
            } catch (Exception e) {
                opcion = -1;
                entrada.nextLine();
            }
            switch (opcion) {
                case 1:
                    System.out.println("Ingrese el título del libro: ");
                    String titulo = entrada.nextLine();
                    try {
                        String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
                        String json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + encodedTitulo);
                        RespuestaLibrosDTO respuestaLibrosDTO = convierteDatos.obtenerDatos(json, RespuestaLibrosDTO.class);
                        List<LibroDTO> librosDTO = respuestaLibrosDTO.getLibros();
                        if (librosDTO.isEmpty()) {
                            System.out.println("Libro no encontrado");
                        } else {
                            boolean libroRegistrado = false;
                            for (LibroDTO libroDTO : librosDTO) {
                                if (libroDTO.getTitulo().equalsIgnoreCase(titulo)) {
                                    Optional<Libro> libroExistente = libroService.obtenerLibroPorTitulo(titulo);
                                    if (libroExistente.isPresent()) {
                                        System.out.println("No se puede registrar un libro más de una vez");
                                        libroRegistrado = true;
                                        break;
                                    } else {
                                        Libro libro = new Libro();
                                        libro.setTitulo(libroDTO.getTitulo());
                                        libro.setIdioma(libroDTO.getIdiomas().get(0));
                                        libro.setNumeroDescargas(libroDTO.getNumeroDescargas());

                                        AutorDTO primerAutorDTO = libroDTO.getAutores().get(0);
                                        Autor autor = autorService.obtenerAutorPorNombre(primerAutorDTO.getNombre())
                                                .orElseGet(() -> {
                                                    Autor nuevoAutor = new Autor();
                                                    nuevoAutor.setNombre(primerAutorDTO.getNombre());
                                                    nuevoAutor.setFechaDeNacimiento(primerAutorDTO.getFechaDeNaciemiento());
                                                    nuevoAutor.setFechaDeFallecimiento(primerAutorDTO.getFechaDeFallecimiento());
                                                    return autorService.crearAutor(nuevoAutor);
                                                });

                                        libro.setAutor(autor);

                                        libroService.crearLibro(libro);
                                        System.out.println("Libro registrado: " + libro.getTitulo());
                                        mostrarDetallesLibro(libroDTO);
                                        libroRegistrado = true;
                                        break;
                                    }
                                }
                            }
                            if (!libroRegistrado) {
                                System.out.println("No se encontró un libro con ese título.");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error al obtener datos de la API");
                    }
                    break;
                case 2:
                    libroService.listarLibros().forEach(libro -> {
                        System.out.println("----LIBRO----");
                        System.out.println("Título: " + libro.getTitulo());
                        System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                        System.out.println("Idioma: " + libro.getIdioma());
                        System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                    });
                    break;
                case 3:
                    autorService.listarAutores().forEach(autor -> {
                        System.out.println("----AUTOR----");
                        System.out.println("Autor: " + autor.getNombre());
                        System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
                        System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
                        String libros = autor.getLibros().stream()
                                .map(Libro::getTitulo)
                                .collect(Collectors.joining(", "));
                        System.out.println("Libros: [ " + libros + "]");
                    });
                    break;
                case 4:
                    System.out.println("Ingrese el año vivo del autor(es) que desea buscar: ");
                    int ano = entrada.nextInt();
                    entrada.nextLine();
                    List<Autor> autoresVivos = autorService.listarAutoresVivosEnAno(ano);
                    if (autoresVivos.isEmpty()) {
                        System.out.println("No se encontraron autores vivos en el año " + ano);
                    } else {
                        autoresVivos.forEach(autor -> {
                            System.out.println("----AUTOR----");
                            System.out.println("Autor: " + autor.getNombre());
                            System.out.println("Fecha de nacimiento: " + autor.getFechaDeNacimiento());
                            System.out.println("Fecha de fallecimiento: " + autor.getFechaDeFallecimiento());
                            System.out.println("Libros: " + autor.getLibros().size());
                        });
                    }
                    break;
                case 5:
                    System.out.println("""
                            Ingrese el idioma:
                            es - Español
                            en - Inglés
                            fr - Francés
                            pt - Portugués
                            """);
                    String idioma = entrada.nextLine();
                    if ("es".equalsIgnoreCase(idioma) || "en".equalsIgnoreCase(idioma) ||
                            "fr".equalsIgnoreCase(idioma) || "pt".equalsIgnoreCase(idioma)) {
                        List<Libro> libros = libroService.listarLibrosPorIdioma(idioma);
                        if (libros.isEmpty()) {
                            System.out.println("No se encontraron libros en ese idioma.");
                        } else {
                            libros.forEach(libro -> {
                                System.out.println("----LIBRO----");
                                System.out.println("Título: " + libro.getTitulo());
                                System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                                System.out.println("Idioma: " + libro.getIdioma());
                                System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                            });
                        }
                    } else {
                        System.out.println("Idioma no válido. Intente nuevamente");
                    }
                    break;
                case 0:
                    System.out.println("Gracias por utilizar esta aplicación!");
                    break;
                default:
                    System.err.println("Opción inválida. Intente nuevamente.");
            }
        }
    }

    private void mostrarDetallesLibro(LibroDTO libroDTO) {
        System.out.println("----LIBRO----");
        System.out.println("Título: " + libroDTO.getTitulo());
        System.out.println("Autor: " + (libroDTO.getAutores().isEmpty() ? "Desconocido" : libroDTO.getAutores().get(0).getNombre()));
        System.out.println("Idioma: " + libroDTO.getIdiomas());
        System.out.println("Número de descargas: " + libroDTO.getNumeroDescargas());
    }
}