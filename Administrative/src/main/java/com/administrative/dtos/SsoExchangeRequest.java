package com.administrative.dtos;

import jakarta.validation.constraints.NotBlank;

public class SsoExchangeRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String target;
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
