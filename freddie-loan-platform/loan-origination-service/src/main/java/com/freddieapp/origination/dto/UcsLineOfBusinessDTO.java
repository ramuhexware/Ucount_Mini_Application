package com.freddieapp.origination.dto;

public class UcsLineOfBusinessDTO {
    private Integer idLiOfBus;
    private String nameLiOfBus;

    public UcsLineOfBusinessDTO() {}

    public UcsLineOfBusinessDTO(Integer idLiOfBus, String nameLiOfBus) {
        this.idLiOfBus = idLiOfBus;
        this.nameLiOfBus = nameLiOfBus;
    }

    public Integer getIdLiOfBus() { return idLiOfBus; }
    public void setIdLiOfBus(Integer idLiOfBus) { this.idLiOfBus = idLiOfBus; }
    public String getNameLiOfBus() { return nameLiOfBus; }
    public void setNameLiOfBus(String nameLiOfBus) { this.nameLiOfBus = nameLiOfBus; }
}
