package com.sequenceiq.cloudbreak.cloud.aws.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.sequenceiq.cloudbreak.cloud.aws.AwsClient;
import com.sequenceiq.cloudbreak.cloud.aws.context.AwsContext;
import com.sequenceiq.cloudbreak.cloud.aws.service.AwsResourceNameService;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource.Builder;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.model.InstanceTemplate;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.Volume;
import com.sequenceiq.cloudbreak.cloud.model.VolumeSetAttributes;
import com.sequenceiq.cloudbreak.common.type.CommonStatus;
import com.sequenceiq.cloudbreak.common.type.ResourceType;

@Component
public class AwsAttachedDiskResourceBuilder extends AbstractAwsComputeBuilder {

    private static final String VOLUME_ID = "id";

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsAttachedDiskResourceBuilder.class);

    @Inject
    private AwsClient awsClient;

    @Override
    public List<CloudResource> create(AwsContext context, long privateId, AuthenticatedContext auth, Group group, Image image) {
        LOGGER.info("Create volume resources");
        AwsResourceNameService resourceNameService = getResourceNameService();

        List<CloudResource> cloudResources = new ArrayList<>();
        CloudInstance instance = group.getReferenceInstanceConfiguration();
        InstanceTemplate template = instance.getTemplate();
        Volume volumeTemplate = template.getVolumes().iterator().next();
        String groupName = group.getName();
        CloudContext cloudContext = auth.getCloudContext();
        String stackName = cloudContext.getName();
        for (int i = 0; i < group.getInstances().size(); i++) {
            cloudResources.add(new Builder()
                    .persistent(true)
                    .type(resourceType())
                    .name(resourceNameService.resourceName(resourceType(), stackName, groupName, privateId, i))
                    .group(group.getName())
                    .status(CommonStatus.REQUESTED)
                    .params(Map.of(CloudResource.ATTRIBUTES, new VolumeSetAttributes.Builder()
                            .withVolumeSize(volumeTemplate.getSize())
                            .withVolumes(template.getVolumes().stream().map(vol -> new VolumeSetAttributes.Volume(null, null, null))
                                    .collect(Collectors.toList()))
                            .build()))
                    .build());
        }

        return cloudResources;
    }

    @Override
    public List<CloudResource> build(AwsContext context, long privateId, AuthenticatedContext auth, Group group,
            List<CloudResource> buildableResource, CloudStack cloudStack) {
        LOGGER.info("Create volumes on provider");

        return List.of();
    }

    @Override
    public CloudResource delete(AwsContext context, AuthenticatedContext auth, CloudResource resource) {
        return null;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.AWS_VOLUMESET;
    }

    @Override
    protected List<CloudResourceStatus> checkResources(ResourceType type, AwsContext context, AuthenticatedContext auth, Iterable<CloudResource> resources) {

        AmazonEC2Client client = awsClient.createAccess(auth.getCloudCredential());
        List<CloudResource> volumeResources = StreamSupport.stream(resources.spliterator(), false)
                .filter(r -> r.getType().equals(resourceType()))
                .collect(Collectors.toList());
        List<String> volumeIds = volumeResources.stream()
                .map(this::getVolumeId)
                .collect(Collectors.toList());

        DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest(volumeIds);
        DescribeVolumesResult result = client.describeVolumes(describeVolumesRequest);
        return result.getVolumes().stream()
                .map(v -> new CloudResourceStatus(findMatchingResource(volumeResources, v.getVolumeId()), toResourceStatus(v.getState())))
                .collect(Collectors.toList());
    }

    private ResourceStatus toResourceStatus(String awsResourceStatus) {
        if("available".equals(awsResourceStatus.toLowerCase())){
            return ResourceStatus.CREATED;
        }
        return ResourceStatus.IN_PROGRESS;
    }

    private String getVolumeId(CloudResource r) {
        return r.getParameter(VOLUME_ID, String.class);
    }

    private CloudResource findMatchingResource(List<CloudResource> volumeResources, String id) {
        return volumeResources.stream().filter(v -> getVolumeId(v).equals(id)).findFirst().orElse(null);
    }
    
    @Override
    public int order() {
        return 1;
    }
}
