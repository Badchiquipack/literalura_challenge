package com.marriaga.alura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutorDTO {

    @JsonProperty("name")
    private String nombre;

    @JsonProperty("birth_year")
    private int fechaDeNaciemiento;

    @JsonProperty("death_year")
    private int fechaDeFallecimiento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getFechaDeNaciemiento() {
        return fechaDeNaciemiento;
    }

    public void setFechaDeNaciemiento(int fechaDeNaciemiento) {
        this.fechaDeNaciemiento = fechaDeNaciemiento;
    }

    public int getFechaDeFallecimiento() {
        return fechaDeFallecimiento;
    }

    public void setFechaDeFallecimiento(int fechaDeFallecimiento) {
        this.fechaDeFallecimiento = fechaDeFallecimiento;
    }
}
