package com.freddieapp.origination.dto;

public class UcsOrgtnRoleDTO {
    private Integer idOrgtnRole;
    private String nameOrgtnRole;

    public UcsOrgtnRoleDTO() {}

    public UcsOrgtnRoleDTO(Integer idOrgtnRole, String nameOrgtnRole) {
        this.idOrgtnRole = idOrgtnRole;
        this.nameOrgtnRole = nameOrgtnRole;
    }

    public Integer getIdOrgtnRole() { return idOrgtnRole; }
    public void setIdOrgtnRole(Integer idOrgtnRole) { this.idOrgtnRole = idOrgtnRole; }
    public String getNameOrgtnRole() { return nameOrgtnRole; }
    public void setNameOrgtnRole(String nameOrgtnRole) { this.nameOrgtnRole = nameOrgtnRole; }
}
