package com.sequenceiq.cloudbreak.core.flow2.cluster.datalake;

import static com.sequenceiq.cloudbreak.api.model.Status.AVAILABLE;
import static com.sequenceiq.cloudbreak.api.model.Status.UPDATE_IN_PROGRESS;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.DetailedStackStatus;
import com.sequenceiq.cloudbreak.core.flow2.stack.FlowMessageService;
import com.sequenceiq.cloudbreak.core.flow2.stack.Msg;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.repository.StackUpdater;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;

@Service
public class EphemeralClusterService {

    @Inject
    private ClusterService clusterService;

    @Inject
    private StackUpdater stackUpdater;

    @Inject
    private FlowMessageService flowMessageService;

    public void updateClusterStarted(long stackId) {
        clusterService.updateClusterStatusByStackId(stackId, UPDATE_IN_PROGRESS);
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.CLUSTER_OPERATION, "Ephemeral cluster update started");
        flowMessageService.fireEventAndLog(stackId, Msg.STACK_DATALAKE_UPDATE, UPDATE_IN_PROGRESS.name());
    }

    public void updateClusterFinished(long stackId) {
        clusterService.updateClusterStatusByStackId(stackId, AVAILABLE);
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.AVAILABLE, "Ephemeral cluster has been updated");
        flowMessageService.fireEventAndLog(stackId, Msg.STACK_DATALAKE_UPDATE_FINISHED, AVAILABLE.name());
    }

    public void updateClusterFailed(Stack stack, Exception exception) {
        clusterService.updateClusterStatusByStackId(stack.getId(), AVAILABLE);
        stackUpdater.updateStackStatus(stack.getId(), DetailedStackStatus.AVAILABLE, "Ephemeral cluster update failed " + exception.getMessage());
        flowMessageService.fireEventAndLog(stack.getId(), Msg.STACK_DATALAKE_UPDATE_FAILED, AVAILABLE.name());
    }
}
