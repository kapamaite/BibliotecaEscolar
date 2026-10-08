import dao.LibroDAO;
import servicio.PrestamoTask;

import java.util.concurrent.atomic.AtomicInteger;

public class PruebaConcurrencia {
    public static void main(String[] args) throws Exception {
        LibroDAO libroDAO = new LibroDAO();
        int idLibro = 3;

        int stockInicial = libroDAO.buscarPorId(idLibro).getStock();
        System.out.println("Stock inicial: " + stockInicial);

        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger fallos = new AtomicInteger();
        Thread[] hilos = new Thread[5];

        for (int i = 0; i < hilos.length; i++) {
            int idEstudiante = i + 1;   // estudiantes 1 al 5
            hilos[i] = new Thread(new PrestamoTask(idEstudiante, idLibro,
                    p -> {
                        exitos.incrementAndGet();
                        System.out.println("✅ Préstamo registrado para estudiante " + p.getIdEstudiante());
                    },
                    msg -> {
                        fallos.incrementAndGet();
                        System.out.println("❌ Rechazado: " + msg);
                    }));
        }

        for (Thread h : hilos) h.start();   // los 5 arrancan "a la vez"
        for (Thread h : hilos) h.join();    // esperamos a que terminen

        System.out.println("Éxitos: " + exitos.get() + " | Rechazados: " + fallos.get());
        System.out.println("Stock final: " + libroDAO.buscarPorId(idLibro).getStock());
    }
}