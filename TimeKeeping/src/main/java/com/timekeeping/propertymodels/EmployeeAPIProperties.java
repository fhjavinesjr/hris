package com.timekeeping.propertymodels;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("api.employee")
public class EmployeeAPIProperties {

    private String login;
    private String register;

    public EmployeeAPIProperties() {

    }

    public EmployeeAPIProperties(String login, String register) {
        this.login = login;
        this.register = register;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }
}
