package com.myorg;

import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.services.ecr.Repository;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;

public class KeeperServiceInfrastructureStack extends Stack {

    public KeeperServiceInfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public KeeperServiceInfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        CfnParameter ecrKeeperRepoName = CfnParameter.Builder.create(this, "ecrKeeperRepoName")
                .build();

        CfnParameter imageTagName = CfnParameter.Builder.create(this, "imageTagName")
                .build();

        Vpc vpc = Vpc.Builder.create(this, "MyVpc")
                .maxAzs(3)
                .build();

        Cluster cluster = Cluster.Builder.create(this, "MyCluster")
                .vpc(vpc)
                .build();

        ApplicationLoadBalancedFargateService.Builder.create(this, "MyFargateService")
                .cluster(cluster)
                .assignPublicIp(true)
                .desiredCount(1)
                .cpu(512)
                .memoryLimitMiB(1024)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(
                                        ContainerImage.fromEcrRepository(
                                                Repository.fromRepositoryName(
                                                        this,
                                                        "EcrKeeperRepo",
                                                        ecrKeeperRepoName.getValueAsString()
                                                ),
                                                imageTagName.getValueAsString()
                                        )
                                )
                                .containerPort(8080)
                                .build())
                .build();
    }
}
