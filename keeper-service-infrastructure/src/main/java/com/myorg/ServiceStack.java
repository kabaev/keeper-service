package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;

import java.util.Map;

public class ServiceStack extends Stack {

    public ServiceStack(
            final Construct scope,
            final String id,
            final StackProps props,
            final NetworkStack networkStack,
            final DataStack dataStack) {
        super(scope, id, props);

        final int CONTAINER_PORT = 8080;
        final int CPU_UNITS_NUMBER = 512;
        final int MEMORY_LIMIT_AMOUNT = 1024;
        final int TASK_INSTANTIATION_NUMBER = 1;

        var ecrKeeperRepoName = CfnParameter.Builder.create(this, "ecrKeeperRepoName")
                .description("The name of the Keeper Service ECR repository keeping the image of the service.")
                .build();

        var imageTagName = CfnParameter.Builder.create(this, "imageTagName")
                .description("The name of the Keeper Service image.")
                .build();

        Vpc vpc = networkStack.getVpc();

        Cluster cluster = Cluster.Builder.create(this, "keeperServiceCluster")
                .vpc(vpc)
                .build();

        var taskRole = Role.Builder.create(this, "taskRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        ApplicationLoadBalancedFargateService.Builder.create(this, "keeperServiceFargate")
                .cluster(cluster)
                .assignPublicIp(true)
                .desiredCount(TASK_INSTANTIATION_NUMBER)
                .cpu(CPU_UNITS_NUMBER)
                .memoryLimitMiB(MEMORY_LIMIT_AMOUNT)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("service")
                                .taskRole(taskRole)
                                .environment(Map.of(
                                        "POSTGRES_HOST", dataStack.getPostgres().getDbInstanceEndpointAddress(),
                                        "POSTGRES_PORT", dataStack.getPostgres().getDbInstanceEndpointPort(),
                                        "POSTGRES_DATABASE", dataStack.getKeeperDatabaseName().getValueAsString()
                                ))
                                .secrets(Map.of(
                                        "POSTGRES_USER", Secret.fromSecretsManager(dataStack.getKeeperDatabaseUserSecret(), "username"),
                                        "POSTGRES_PASSWORD", Secret.fromSecretsManager(dataStack.getKeeperDatabaseUserSecret(), "password")
                                ))
                                .image(
                                        ContainerImage.fromEcrRepository(
                                                Repository.fromRepositoryName(
                                                        this,
                                                        "ecrKeeperRepo",
                                                        ecrKeeperRepoName.getValueAsString()
                                                ),
                                                imageTagName.getValueAsString()
                                        )
                                )
                                .containerPort(CONTAINER_PORT)
                                .build())
                .build();
    }

}
