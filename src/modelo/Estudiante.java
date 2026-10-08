package modelo;

public class Estudiante {
    private int id;
    private String nombre;
    private String rut;
    private String curso;
    private String correo;

    public Estudiante() {}
    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.curso = curso;
        this.correo = correo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}