package com.freddieapp.origination.dto;

public class AccountSaveDTO {
    private String idCntprtyAcct;
    private String orgName;
    private String accountType;
    private String status;
    private ResponseStatusDTO respSts;

    public AccountSaveDTO() {}

    public AccountSaveDTO(String idCntprtyAcct, String orgName, String accountType, String status) {
        this.idCntprtyAcct = idCntprtyAcct;
        this.orgName = orgName;
        this.accountType = accountType;
        this.status = status;
    }

    public String getIdCntprtyAcct() { return idCntprtyAcct; }
    public void setIdCntprtyAcct(String idCntprtyAcct) { this.idCntprtyAcct = idCntprtyAcct; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ResponseStatusDTO getRespSts() { return respSts; }
    public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
}
