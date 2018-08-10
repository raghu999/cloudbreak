package com.sequenceiq.cloudbreak.cloud.cumulus.yarn;

import static com.sequenceiq.cloudbreak.common.type.ResourceType.CUMULUS_YARN_SERVICE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.cb.yarn.service.api.ApiException;
import org.apache.cb.yarn.service.api.ApiResponse;
import org.apache.cb.yarn.service.api.impl.DefaultApi;
import org.apache.cb.yarn.service.api.records.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.cloud.ResourceConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.exception.CloudOperationNotSupportedException;
import com.sequenceiq.cloudbreak.cloud.exception.TemplatingDoesNotSupportedException;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.TlsInfo;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.cumulus.yarn.client.ServiceCreator;
import com.sequenceiq.cloudbreak.cloud.cumulus.yarn.client.CumulusYarnClient;
import com.sequenceiq.cloudbreak.cloud.cumulus.yarn.client.CumulusYarnResourceConstants;

@org.springframework.stereotype.Service
public class CumulusYarnResourceConnector implements ResourceConnector<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CumulusYarnResourceConnector.class);

    @Inject
    private CumulusYarnClient client;

    @Inject
    private ServiceCreator serviceCreator;

    @Override
    public List<CloudResourceStatus> launch(AuthenticatedContext authenticatedContext, CloudStack stack, PersistenceNotifier persistenceNotifier,
            AdjustmentType adjustmentType, Long threshold) throws Exception {
        DefaultApi api = client.createApi(authenticatedContext);
        Service service = serviceCreator.create(authenticatedContext, stack);
        ApiResponse<Void> response = api.appV1ServicesPostWithHttpInfo(service);

        if (response.getStatusCode() != CumulusYarnResourceConstants.HTTP_ACCEPTED) {
            throw new CloudConnectorException(String.format("Yarn Service creation error: HTTP Return: %d", response.getStatusCode()));
        }

        CloudResource yarnService = new CloudResource.Builder().type(CUMULUS_YARN_SERVICE).name(service.getName()).build();
        persistenceNotifier.notifyAllocation(yarnService, authenticatedContext.getCloudContext());
        return check(authenticatedContext, Collections.singletonList(yarnService));
    }

    @Override
    public List<CloudResourceStatus> check(AuthenticatedContext authenticatedContext, List<CloudResource> resources) {
        DefaultApi api = client.createApi(authenticatedContext);
        List<CloudResourceStatus> result = new ArrayList<>();
        for (CloudResource resource : resources) {
            switch (resource.getType()) {
                case CUMULUS_YARN_SERVICE:
                    LOGGER.info("Checking Yarn application status of: {}", resource.getName());
                    try {
                        ApiResponse<Service> response = api.appV1ServicesServiceNameGetWithHttpInfo(resource.getName());
                        if (response.getStatusCode() == CumulusYarnResourceConstants.HTTP_SUCCESS) {
                            Service service = response.getData();
                            result.add(new CloudResourceStatus(resource,
                                    CumulusYarnApplicationStatus.mapResourceStatus(service.getState())));
                        } else if (response.getStatusCode() == CumulusYarnResourceConstants.HTTP_NOT_FOUND) {
                            result.add(new CloudResourceStatus(resource, ResourceStatus.DELETED, "Yarn application has been killed."));
                        } else if (response.getData() != null) {
                            throw new CloudConnectorException(String.format("Yarn Application status check failed: HttpStatusCode: %d, Error: %s",
                                    response.getStatusCode(), response.getData()));
                        } else {
                            throw new CloudConnectorException(String.format("Yarn Application status check failed: Invalid HttpStatusCode: %d",
                                    response.getStatusCode()));
                        }
                    } catch (ApiException | RuntimeException e) {
                        throw new CloudConnectorException(String.format("Invalid resource exception: %s", e.getMessage()), e);
                    }
                    break;
                default:
                    throw new CloudConnectorException(String.format("Invalid resource type: %s", resource.getType()));
            }
        }
        return result;
    }

    @Override
    public List<CloudResourceStatus> terminate(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> cloudResources) {
        List<CloudResourceStatus> cloudResourceStatuses = new ArrayList<>();
        for (CloudResource resource : cloudResources) {
            switch (resource.getType()) {
                case CUMULUS_YARN_SERVICE:
                    DefaultApi api = client.createApi(authenticatedContext);
                    String yarnApplicationName = resource.getName();
                    String stackName = authenticatedContext.getCloudContext().getName();
                    LOGGER.info("Terminate stack: {}", stackName);
                    try {
                        ApiResponse<Void> response = api.appV1ServicesServiceNameDeleteWithHttpInfo(yarnApplicationName);
                        LOGGER.info("Yarn Service has been deleted");
                        if (response.getStatusCode() == CumulusYarnResourceConstants.HTTP_ACCEPTED
                                || response.getStatusCode() == CumulusYarnResourceConstants.HTTP_NO_CONTENT
                                || response.getStatusCode() == CumulusYarnResourceConstants.HTTP_SUCCESS) {
                            String msg = String.format("Successfully deleted application %s", yarnApplicationName);
                            LOGGER.debug(msg);
                        } else if (response.getStatusCode() == CumulusYarnResourceConstants.HTTP_NOT_FOUND) {
                            String msg = String.format("Application %s not found, already deleted?", yarnApplicationName);
                            LOGGER.debug(msg);
                        } else {
                            if (response.getData() != null) {
                                throw new CloudConnectorException(String.format("Yarn Application status check failed: HttpStatusCode: %d, Error: %s",
                                        response.getStatusCode(), response.getData()));
                            } else {
                                throw new CloudConnectorException(String.format("Yarn Application status check failed: Invalid HttpStatusCode: %d",
                                        response.getStatusCode()));
                            }
                        }
                        cloudResourceStatuses.add(new CloudResourceStatus(resource, ResourceStatus.DELETED));
                    } catch (ApiException e) {
                        throw new CloudConnectorException("Stack cannot be deleted", e);
                    }
                    break;
                default:
                    throw new CloudConnectorException(String.format("Invalid resource type: %s", resource.getType()));
            }
        }
        return cloudResourceStatuses;
    }

    @Override
    public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) throws Exception {
        return null;
    }

    @Override
    public List<CloudResourceStatus> upscale(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) {
        throw new CloudOperationNotSupportedException("Upscale stack operation is not supported on YARN");
    }

    @Override
    public List<CloudResourceStatus> downscale(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources,
            List<CloudInstance> vms, Object resourcesToRemove) {
        throw new CloudOperationNotSupportedException("Downscale stack operation is not supported on YARN");
    }

    @Override
    public Object collectResourcesToRemove(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources,
            List<CloudInstance> vms) {
        throw new CloudOperationNotSupportedException("Downscale resources collection operation is not supported on YARN");
    }

    @Override
    public TlsInfo getTlsInfo(AuthenticatedContext authenticatedContext, CloudStack cloudStack) {
        return new TlsInfo(false);
    }

    @Override
    public String getStackTemplate() throws TemplatingDoesNotSupportedException {
        throw new TemplatingDoesNotSupportedException();
    }
}
