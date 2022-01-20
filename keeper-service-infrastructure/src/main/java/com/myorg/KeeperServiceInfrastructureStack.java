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

//        CfnParameter keeperRepoName = CfnParameter.Builder.create(this, "keeper-repo")
//                .build();

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
                                                        "keeper-repo-id",
                                                        "keeper-repo"

                                                ),
                                                "f8cbfd8b143123abc8e0c60e8995d02c2f74e20c"

                                        )
//                                        ContainerImage.fromRegistry("epamtimurkabaev/keeper-service")
                                )
                                .containerPort(8081)
                                .build())
                .build();
    }
}
