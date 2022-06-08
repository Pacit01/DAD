package Clases;


import java.util.Objects;
public class Pistas {
	private Integer pistaId;
	private String nombre;
	private Integer longitud;
	private Long fecha;
	private Boolean apertura;
	private Integer capacidadMax;
	private String dificultad;
	public Pistas(Integer pistaId,String nombre, Integer longitud, long fecha, Boolean apertura, Integer capacidadMax, String dificultad
			) {
		super();
		this.pistaId=pistaId;
		this.nombre = nombre;
		this.longitud = longitud;
		this.fecha = fecha;
		this.apertura = apertura;
		this.capacidadMax = capacidadMax;
		this.dificultad = dificultad;
		
		
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Integer getLongitud() {
		return longitud;
	}
	public void setLongitud(Integer longitud) {
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
	public Integer getCapacidadMax() {
		return capacidadMax;
	}
	public void setCapacnombresIdadMax(Integer capacnombresIdadMax) {
		this.capacidadMax = capacnombresIdadMax;
	}
	public String getDificultad() {
		return dificultad;
	}
	public void setDificultad(String dificultad) {
		this.dificultad = dificultad;
	}
	
	public Integer getPistaId() {
		return pistaId;
	}
	public void setPistaId(Integer pistaId) {
		this.pistaId = pistaId;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(apertura, capacidadMax, dificultad,longitud, nombre, fecha, pistaId);
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
				&& Objects.equals(fecha, other.fecha) && Objects.equals(pistaId, other.pistaId);
	}
	@Override
	public String toString() {
		return "Pistas [pistasId=" + pistaId + ",nombre=" + nombre + "groupId=" + longitud + ", fecha=" + fecha + ", apertura=" + apertura
				+ ", capacnombresIdadMax=" + capacidadMax + ", dificultad=" + dificultad + "]";
	}
	
}