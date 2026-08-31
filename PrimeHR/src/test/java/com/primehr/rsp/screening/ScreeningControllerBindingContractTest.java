package com.primehr.rsp.screening;

import com.primehr.rsp.screening.api.ScreeningCaseController;
import com.primehr.rsp.screening.api.ScreeningPolicyController;
import com.primehr.rsp.api.RspPlanningController;
import com.primehr.rsp.api.RspPublicationController;
import com.primehr.rsp.applicant.api.ApplicantApplicationController;
import com.primehr.rsp.applicant.api.ApplicantSelfServiceController;
import com.primehr.rsp.applicant.api.PublicApplicantController;
import com.primehr.rsp.applicant.api.RspApplicantIntakeController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreeningControllerBindingContractTest {

    @Test
    void everyPathAndQueryParameterHasAnExplicitHttpName() {
        for (Class<?> controller : List.of(ScreeningPolicyController.class, ScreeningCaseController.class,
                ApplicantApplicationController.class, ApplicantSelfServiceController.class,
                PublicApplicantController.class, RspApplicantIntakeController.class,
                RspPlanningController.class, RspPublicationController.class)) {
            for (Method method : controller.getDeclaredMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    PathVariable path = parameter.getAnnotation(PathVariable.class);
                    if (path != null) {
                        assertThat(path.value()).as("%s#%s path variable", controller.getSimpleName(), method.getName())
                                .isNotBlank();
                    }
                    RequestParam query = parameter.getAnnotation(RequestParam.class);
                    if (query != null) {
                        assertThat(!query.name().isBlank() || !query.value().isBlank())
                                .as("%s#%s query parameter", controller.getSimpleName(), method.getName()).isTrue();
                    }
                }
            }
        }
    }
}
