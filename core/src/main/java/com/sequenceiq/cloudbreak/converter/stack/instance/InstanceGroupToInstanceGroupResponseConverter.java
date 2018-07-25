package com.sequenceiq.cloudbreak.converter.stack.instance;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceMetaDataJson;
import com.sequenceiq.cloudbreak.api.model.SecurityGroupResponse;
import com.sequenceiq.cloudbreak.api.model.TemplateResponse;
import com.sequenceiq.cloudbreak.api.model.v2.availability.AvailabilityResponse;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.stack.availability.AvailabilityConfig;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.json.Json;

@Component
public class InstanceGroupToInstanceGroupResponseConverter extends AbstractConversionServiceAwareConverter<InstanceGroup, InstanceGroupResponse> {

    @Override
    public InstanceGroupResponse convert(InstanceGroup entity) {
        InstanceGroupResponse instanceGroupResponse = new InstanceGroupResponse();
        instanceGroupResponse.setGroup(entity.getGroupName());
        instanceGroupResponse.setId(entity.getId());
        instanceGroupResponse.setNodeCount(entity.getNodeCount());
        if (entity.getTemplate() != null) {
            instanceGroupResponse.setTemplateId(entity.getTemplate().getId());
            instanceGroupResponse.setTemplate(getConversionService().convert(entity.getTemplate(), TemplateResponse.class));
        }
        instanceGroupResponse.setType(entity.getInstanceGroupType());
        instanceGroupResponse.setMetadata(convertEntitiesToJson(entity.getNotDeletedInstanceMetaDataSet()));
        if (entity.getSecurityGroup() != null) {
            instanceGroupResponse.setSecurityGroup(getConversionService().convert(entity.getSecurityGroup(), SecurityGroupResponse.class));
            instanceGroupResponse.setSecurityGroupId(entity.getSecurityGroup().getId());
        }
        Json attributes = entity.getAttributes();
        if (attributes != null) {
            instanceGroupResponse.setParameters(attributes.getMap());
        }
        instanceGroupResponse.setAvailability(new HashSet<>());
        if (entity.getAvailabilityConfigs() != null) {
            for (AvailabilityConfig availabilityConfig : entity.getAvailabilityConfigs()) {
                instanceGroupResponse.getAvailability().add(getConversionService().convert(availabilityConfig, AvailabilityResponse.class));
            }
        }
        return instanceGroupResponse;
    }

    private Set<InstanceMetaDataJson> convertEntitiesToJson(Set<InstanceMetaData> metadata) {
        return (Set<InstanceMetaDataJson>) getConversionService().convert(metadata,
                TypeDescriptor.forObject(metadata),
                TypeDescriptor.collection(Set.class, TypeDescriptor.valueOf(InstanceMetaDataJson.class)));
    }

}
