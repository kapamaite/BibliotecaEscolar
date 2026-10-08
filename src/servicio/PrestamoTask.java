package servicio;

import modelo.Prestamo;

import java.util.function.Consumer;

// Tarea que se ejecuta en un hilo aparte para no congelar la interfaz
public class PrestamoTask implements Runnable {

    private final int idEstudiante;
    private final int idLibro;
    private final Consumer<Prestamo> alExito;   // qué hacer si salió bien
    private final Consumer<String> alError;     // qué hacer si falló

    public PrestamoTask(int idEstudiante, int idLibro,
                        Consumer<Prestamo> alExito, Consumer<String> alError) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.alExito = alExito;
        this.alError = alError;
    }

    @Override
    public void run() {
        try {
            Prestamo p = PrestamoService.registrarPrestamo(idEstudiante, idLibro);
            alExito.accept(p);
        } catch (Exception e) {
            alError.accept(e.getMessage());
        }
    }
}