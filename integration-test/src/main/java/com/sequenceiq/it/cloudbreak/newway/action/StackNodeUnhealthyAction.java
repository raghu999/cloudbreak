package com.sequenceiq.it.cloudbreak.newway.action;

import com.sequenceiq.cloudbreak.api.model.FailureReport;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceMetaDataJson;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.StackEntity;
import com.sequenceiq.it.cloudbreak.newway.actor.Actor;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sequenceiq.it.cloudbreak.newway.CloudbreakTest.SECOND_USER;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;
import static java.lang.String.format;

public class StackNodeUnhealthyAction implements ActionV2<StackEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackNodeUnhealthyAction.class);

    private final String hostgroup;

    private final int nodeCount;

    public StackNodeUnhealthyAction(String hostgroup, int nodeCount) {
        this.hostgroup = hostgroup;
        this.nodeCount = nodeCount;
    }

    @Override
    public StackEntity action(TestContext testContext, StackEntity entity, CloudbreakClient client) throws Exception {
        log(LOGGER, format(" Name: %s", entity.getRequest().getGeneral().getName()));
        logJSON(LOGGER, format(" Stack unhealthy request:%n"), entity.getRequest());
        FailureReport failureReport = new FailureReport();
        failureReport.setFailedNodes(getNodes(getInstanceGroupResponse(entity)));
        CloudbreakClient autoscaleClient = testContext.as(Actor::secondUser).getCloudbreakClient(SECOND_USER);
        try (Response toClose = autoscaleClient.getCloudbreakClient().autoscaleEndpoint()
                .failureReport(Objects.requireNonNull(entity.getResponse().getId()), failureReport)) {
            logJSON(LOGGER, format(" Stack unhealthy was successful:%n"), entity.getResponse());
            log(LOGGER, format(" ID: %s", entity.getResponse().getId()));
            return entity;
        }
    }

    private InstanceGroupResponse getInstanceGroupResponse(StackEntity entity) {
        return entity.getResponse().getInstanceGroups().stream()
                .filter(ig -> ig.getGroup().equals(hostgroup)).collect(Collectors.toList()).get(0);
    }

    private List<String> getNodes(InstanceGroupResponse instanceGroup) {
        return instanceGroup.getMetadata().stream()
                .map(InstanceMetaDataJson::getDiscoveryFQDN).collect(Collectors.toList()).subList(0, nodeCount);
    }

}