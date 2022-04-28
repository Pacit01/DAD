package es.us.lsi.dad;

import java.util.Objects;

public class Termometro {
	private Integer termometroId;
	private Double temperatura;
	private Double humedad;
	

	public Termometro(Integer termometroId, Double temperatura, Double humedad) {
		super();
		this.termometroId = termometroId;
		this.temperatura = temperatura;
		this.humedad = humedad;
	}


	public Termometro() {
		super();
	}


	public Integer getTermometroId() {
		return termometroId;
	}


	public void setTermometroId(Integer termometroId) {
		this.termometroId = termometroId;
	}


	public Double getTemperatura() {
		return temperatura;
	}


	public void setTemperatura(Double temperatura) {
		temperatura = temperatura;
	}


	public Double getHumedad() {
		return humedad;
	}


	public void setHumedad(Double humedad) {
		this.humedad = humedad;
	}

	@Override
	public int hashCode() {
		return Objects.hash(temperatura,humedad);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Termometro other = (Termometro) obj;
		return  Objects.equals(temperatura, other.temperatura) && Objects.equals(humedad, other.humedad) 
				 && Objects.equals(termometroId, other.termometroId);
	}


	@Override
	public String toString() {
		return "Termometro [termometroId=" + termometroId + ", temperatura=" + temperatura + ", humedad=" + humedad + "]";
	}
	
}