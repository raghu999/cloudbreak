package com.sequenceiq.cloudbreak.controller;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v1.AuditEndpoint;
import com.sequenceiq.cloudbreak.api.model.audit.AuditEvent;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.service.AuthenticatedUserService;
import com.sequenceiq.cloudbreak.service.audit.AuditEventService;

@Component
@Transactional(TxType.NEVER)
public class AuditController implements AuditEndpoint {

    @Inject
    private AuthenticatedUserService authenticatedUserService;

    @Inject
    private AuditEventService auditEventService;

    @Override
    public List<AuditEvent> getAuditEvents(String resourceType, Long resourceId) {
        IdentityUser identityUser = authenticatedUserService.getCbUser();
        return auditEventService.getAuditEvents(identityUser.getUserId(), resourceType, resourceId);
    }
}
