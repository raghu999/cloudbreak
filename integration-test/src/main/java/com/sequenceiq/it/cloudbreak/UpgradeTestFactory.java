package com.sequenceiq.it.cloudbreak;

import com.sequenceiq.it.cloudbreak.newway.CloudbreakTest;
import com.sequenceiq.it.cloudbreak.newway.cloud.CloudProvider;
import com.sequenceiq.it.cloudbreak.newway.cloud.GcpCloudProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.util.stream.IntStream;

import static com.sequenceiq.it.cloudbreak.newway.cloud.CloudProviderHelper.providerFactory;

public class UpgradeTestFactory extends CloudbreakTest {
    @Factory
    @Parameters({"provider"})
    public Object[] clusterTestFactory(@Optional(GcpCloudProvider.GCP) String provider) {
        CloudProvider[] providers = providerFactory(provider);
        Object[] results = new Object[providers.length];
        IntStream.range(0, providers.length).forEach(index -> results[index] = new ClusterTests(providers[index]));
        return results;
    }
}