package com.kabaev.shop.service.keeper.infrastructure;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;

import java.util.List;
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
                .enableFargateCapacityProviders(true)
                .build();

        var taskRole = Role.Builder.create(this, "taskRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        var logGroup = LogGroup.Builder.create(this, getStackName() + "LogGroup")
                .retention(RetentionDays.ONE_DAY)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        CfnOutput.Builder.create(this, "TaskLogGroupName")
                .description("Logs from the main container")
                .value(logGroup.getLogGroupName())
                .build();

        var taskDefinition = FargateTaskDefinition.Builder.create(this, "taskDefinition")
                .taskRole(taskRole)
                .memoryLimitMiB(MEMORY_LIMIT_AMOUNT)
                .cpu(CPU_UNITS_NUMBER)
                .build();

        taskDefinition.addContainer(
                "serviceContainer",
                ContainerDefinitionOptions.builder()
                        .containerName("service")
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
                        .cpu(CPU_UNITS_NUMBER)
                        .memoryLimitMiB(MEMORY_LIMIT_AMOUNT)
                        .environment(Map.of(
                                "POSTGRES_HOST", dataStack.getPostgres().getDbInstanceEndpointAddress(),
                                "POSTGRES_PORT", dataStack.getPostgres().getDbInstanceEndpointPort(),
                                "POSTGRES_DATABASE", dataStack.getKeeperDatabaseName().getValueAsString(),
                                "S3_REGION_NAME", getRegion(),
                                "S3_BUCKET_NAME", dataStack.getImagesStoreBucket().getBucketName(),
                                "SNS_TOPIC_ARN", dataStack.getTopic().getTopicArn()
                        ))
                        .secrets(Map.of(
                                "POSTGRES_USER", Secret.fromSecretsManager(dataStack.getKeeperDatabaseUserSecret(), "username"),
                                "POSTGRES_PASSWORD", Secret.fromSecretsManager(dataStack.getKeeperDatabaseUserSecret(), "password")
                        ))
                        .portMappings(List.of(PortMapping.builder().containerPort(CONTAINER_PORT).build()))
                        .logging(LogDriver.awsLogs(
                                AwsLogDriverProps.builder()
                                        .streamPrefix(getStackName())
                                        .logGroup(logGroup)
                                        .build()
                        ))
                        .build()
        );

        var loadBalancer = ApplicationLoadBalancer.Builder.create(this, "loadBalancer")
                .vpc(vpc)
                .internetFacing(true)
                .build();

        loadBalancer.logAccessLogs(dataStack.getImagesStoreBucket());

        CfnOutput.Builder.create(this, "keeperAppEndpoint")
                .description("HTTP Endpoint")
                .value("http://" + loadBalancer.getLoadBalancerDnsName())
                .build();

        var keeperService = FargateService.Builder.create(this, "keeperService")
                .cluster(cluster)
                .assignPublicIp(true)
                .taskDefinition(taskDefinition)
                .circuitBreaker(DeploymentCircuitBreaker.builder().rollback(true).build())
                .desiredCount(TASK_INSTANTIATION_NUMBER)
                .build();

        var targetGroup = ApplicationTargetGroup.Builder.create(this, "targetGroup")
                .vpc(vpc)
                .protocol(ApplicationProtocol.HTTP)
                .targets(List.of(keeperService))
                .healthCheck(
                        HealthCheck.builder()
                                .path("/actuator/health")
                                .healthyThresholdCount(2)
                                .unhealthyThresholdCount(5)
                                .build()
                )
                .build();

        var listener = loadBalancer.addListener(
                "keeperListener",
                BaseApplicationListenerProps.builder()
                        .port(80)
                        .defaultAction(ListenerAction.fixedResponse(404))
                        .build()
        );

        listener.addAction(
                "forwardAction",
                AddApplicationActionProps.builder()
                        .priority(100)
                        .conditions(List.of(ListenerCondition.pathPatterns(List.of("/api/v1/*"))))
                        .action(ListenerAction.forward(List.of(targetGroup)))
                        .build()
        );

        dataStack.getImagesStoreBucket().grantReadWrite(taskRole);
        dataStack.getTopic().grantPublish(taskRole);
    }

}
