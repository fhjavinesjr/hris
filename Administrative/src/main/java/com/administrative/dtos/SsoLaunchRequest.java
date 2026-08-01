package com.administrative.dtos;

import jakarta.validation.constraints.NotBlank;

public class SsoLaunchRequest {
    @NotBlank
    private String target;
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
