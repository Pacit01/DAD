package es.us.lsi.dad;


import java.util.Objects;
public class Pistas {
	private int pistasId;
	private int termometroId;
	private String nombre;
	private int longitud;
	private long fecha;
	private Boolean apertura;
	private int capacidadMax;
	private String dificultad;
	public Pistas(int pistasId,int termometroId, String nombre, int longitud, long fecha, Boolean apertura, int capacidadMax, String dificultad) {
		super();
		this.pistasId = pistasId;
		this.termometroId = termometroId;
		this.nombre = nombre;
		this.longitud = longitud;
		this.fecha = fecha;
		this.apertura = apertura;
		this.capacidadMax = capacidadMax;
		this.dificultad = dificultad;	
	
	}
	public int getPistasId() {
		return pistasId;
	}
	public void setPistasId(int pistasId) {
		this.pistasId = pistasId;
	}
	
	public int getTermometroId() {
		return termometroId;
	}
	public void setTermometroId(int termometroId) {
		this.termometroId = termometroId;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public int getLongitud() {
		return longitud;
	}
	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}
	public long getFecha() {
		return fecha;
	}
	public void setFecha(long fecha) {
		this.fecha = fecha;
	}
	public Boolean getApertura() {
		return apertura;
	}
	public void setApertura(Boolean apertura) {
		this.apertura = apertura;
	}
	public int getCapacidadMax() {
		return capacidadMax;
	}
	public void setCapacidadMax(int capacidadMax) {
		this.capacidadMax = capacidadMax;
	}
	public String getDificultad() {
		return dificultad;
	}
	public void setDificultad(String dificultad) {
		this.dificultad = dificultad;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(apertura, capacidadMax, dificultad,longitud, nombre, fecha);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pistas other = (Pistas) obj;
		return Objects.equals(apertura, other.apertura) && capacidadMax == other.capacidadMax
				&& Objects.equals(dificultad, other.dificultad)
				&& longitud == other.longitud && Objects.equals(nombre, other.nombre)
				&& Objects.equals(fecha, other.fecha);
	}
	@Override
	public String toString() {
		return "Pistas [pista=" + nombre + ", longitud=" + longitud + ", tiempo=" + fecha + ", apertura=" + apertura
				+ ", capacidadMax=" + capacidadMax + ", dificultad=" + dificultad + "]";
	}
	
}