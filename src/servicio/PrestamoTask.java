package servicio;

import modelo.Prestamo;

import java.util.function.Consumer;

// Tarea que se ejecuta en un hilo aparte para no congelar la interfaz
public class PrestamoTask implements Runnable {

    // Pausa artificial SOLO para demostrar que la ventana no se congela.
    // Ponla en 0 si quieres quitarla.
    private static final int RETRASO_DEMO_MS = 1500;

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
            if (RETRASO_DEMO_MS > 0) {
                Thread.sleep(RETRASO_DEMO_MS);
            }
            Prestamo p = PrestamoService.registrarPrestamo(idEstudiante, idLibro);
            alExito.accept(p);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alError.accept("La operación fue interrumpida.");
        } catch (Exception e) {
            alError.accept(e.getMessage());
        }
    }
}