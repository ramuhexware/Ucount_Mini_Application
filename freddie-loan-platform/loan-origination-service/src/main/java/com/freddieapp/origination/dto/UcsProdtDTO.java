package com.freddieapp.origination.dto;

public class UcsProdtDTO {
    private Integer idProdt;
    private String nameProdt;

    public UcsProdtDTO() {}

    public UcsProdtDTO(Integer idProdt, String nameProdt) {
        this.idProdt = idProdt;
        this.nameProdt = nameProdt;
    }

    public Integer getIdProdt() { return idProdt; }
    public void setIdProdt(Integer idProdt) { this.idProdt = idProdt; }
    public String getNameProdt() { return nameProdt; }
    public void setNameProdt(String nameProdt) { this.nameProdt = nameProdt; }
}
