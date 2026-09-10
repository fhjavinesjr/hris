package com.humanresource.onboarding;

import com.humanresource.onboarding.OnboardingDtos.ActivationCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/employee")
public class EmployeeActivationController {
    private final OnboardingService service;public EmployeeActivationController(OnboardingService s){service=s;}
    @PostMapping("/activate")@ResponseStatus(HttpStatus.NO_CONTENT) public void activate(@Valid @RequestBody ActivationCommand command){service.activate(command);}
}
