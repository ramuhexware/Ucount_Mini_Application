package com.freddieapp.origination.dto;

import java.util.List;
import java.util.Map;

public class AccountLookupUpdateDTO {
    private List<UcsLineOfBusinessDTO> ucsLineOfBusinessDTOs;
    private Map<String, List<UcsOrgtnRoleDTO>> ucsOrgtnRoleDTOs;
    private Map<String, List<UcsProdtDTO>> ucsProdtDTOs;
    private List<String> ucsCntprtyAcctSt;
    private List<String> ucsCntprtyAcctRoleSt;
    private ResponseStatusDTO respSts;

    public AccountLookupUpdateDTO() {}

    public List<UcsLineOfBusinessDTO> getUcsLineOfBusinessDTOs() { return ucsLineOfBusinessDTOs; }
    public void setUcsLineOfBusinessDTOs(List<UcsLineOfBusinessDTO> dtos) { this.ucsLineOfBusinessDTOs = dtos; }
    public Map<String, List<UcsOrgtnRoleDTO>> getUcsOrgtnRoleDTOs() { return ucsOrgtnRoleDTOs; }
    public void setUcsOrgtnRoleDTOs(Map<String, List<UcsOrgtnRoleDTO>> map) { this.ucsOrgtnRoleDTOs = map; }
    public Map<String, List<UcsProdtDTO>> getUcsProdtDTOs() { return ucsProdtDTOs; }
    public void setUcsProdtDTOs(Map<String, List<UcsProdtDTO>> map) { this.ucsProdtDTOs = map; }
    public List<String> getUcsCntprtyAcctSt() { return ucsCntprtyAcctSt; }
    public void setUcsCntprtyAcctSt(List<String> list) { this.ucsCntprtyAcctSt = list; }
    public List<String> getUcsCntprtyAcctRoleSt() { return ucsCntprtyAcctRoleSt; }
    public void setUcsCntprtyAcctRoleSt(List<String> list) { this.ucsCntprtyAcctRoleSt = list; }
    public ResponseStatusDTO getRespSts() { return respSts; }
    public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
}
