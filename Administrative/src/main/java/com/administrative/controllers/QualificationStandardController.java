package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.QualificationStandardDtos.*;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.services.EffectiveAuthorizationService;
import com.administrative.services.QualificationStandardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/qualification-standards")
public class QualificationStandardController {
    private final QualificationStandardService service; private final EffectiveAuthorizationService authorization;
    public QualificationStandardController(QualificationStandardService s,EffectiveAuthorizationService a){service=s;authorization=a;}
    @GetMapping public List<Response> list(Authentication a,@RequestParam Long jobPositionId){require(a,"access");return service.list(jobPositionId);}
    @GetMapping("/{id}") public Response get(Authentication a,@PathVariable Long id){require(a,"access");return service.get(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Response create(Authentication a,@Valid @RequestBody Save r){require(a,"add");return service.create(r,a.getName());}
    @PutMapping("/{id}") public Response update(Authentication a,@PathVariable Long id,@Valid @RequestBody Save r){require(a,"edit");return service.update(id,r,a.getName());}
    @PostMapping("/{id}/archive") public Response archive(Authentication a,@PathVariable Long id,@Valid @RequestBody Transition r){require(a,"delete");return service.archive(id,r,a.getName());}
    @PostMapping("/{id}/versions") @ResponseStatus(HttpStatus.CREATED) public Response successor(Authentication a,@PathVariable Long id,@Valid @RequestBody Transition r){require(a,"add");return service.successor(id,r,a.getName());}
    @PostMapping("/{id}/publish") public Response publish(Authentication a,@PathVariable Long id,@Valid @RequestBody Transition r){require(a,"publish");return service.publish(id,r,a.getName());}
    private void require(Authentication a,String action){if(a==null||!a.isAuthenticated())throw new AccessDeniedException("Authentication is required");String role=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");
        EffectiveFeaturePermissionResponse p=authorization.resolve(a.getName(),role,EffectiveAuthorizationServiceImpl.ADMIN_QUALIFICATION_STANDARD);
        boolean allowed=p.administrator()||p.canAccess()&&switch(action){case "access"->true;case "add"->p.canAdd();case "edit"->p.canEdit();case "delete"->p.canDelete();case "publish"->p.canPublish();default->false;};if(!allowed)throw new AccessDeniedException("Qualification Standard action is not permitted");}
}
