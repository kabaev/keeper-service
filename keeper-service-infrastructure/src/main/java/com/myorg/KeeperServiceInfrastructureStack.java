package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;

import java.util.Map;

public class KeeperServiceInfrastructureStack extends Stack {

    public KeeperServiceInfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public KeeperServiceInfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final int AVAILABILITY_ZONE_NUMBER = 3;
        final int ALLOCATED_DATABASE_STORAGE_NUMBER = 20;
        final int CONTAINER_PORT = 8080;
        final int DATABASE_PORT = 5432;
        final int CPU_UNITS_NUMBER = 512;
        final int MEMORY_LIMIT_AMOUNT = 1024;
        final int TASK_INSTANTIATION_NUMBER = 1;

        var ecrKeeperRepoName = CfnParameter.Builder.create(this, "ecrKeeperRepoName")
                .description("The name of the Keeper Service ECR repository keeping the image of the service.")
                .build();

        var imageTagName = CfnParameter.Builder.create(this, "imageTagName")
                .description("The name of the Keeper Service image.")
                .build();

        Vpc vpc = Vpc.Builder.create(this, "VPC")
                .maxAzs(AVAILABILITY_ZONE_NUMBER)
                .subnetConfiguration(Vpc.DEFAULT_SUBNETS_NO_NAT)
                .build();

        var keeperDatabaseName = CfnParameter.Builder.create(this, "keeperDatabaseName")
                .description("The name of the Keeper Service Database keeping information about all products.")
                .defaultValue("keeper_db")
                .build();

        var keeperDatabaseUserName = CfnParameter.Builder.create(this, "keeperDatabaseUserName")
                .description("The name of the Keeper Service Database user.")
                .defaultValue("user_db")
                .build();

        var keeperDatabaseUserSecret = DatabaseSecret.Builder.create(this, "keeperDatabaseUserSecret")
                .username(keeperDatabaseUserName.getValueAsString())
                .build();

        var postgres = DatabaseInstance.Builder.create(this, "Postgres")
                .engine(
                        DatabaseInstanceEngine.postgres(
                                PostgresInstanceEngineProps.builder()
                                        .version(PostgresEngineVersion.VER_13_4)
                                        .build()
                        )
                )
                .credentials(Credentials.fromSecret(keeperDatabaseUserSecret))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .allocatedStorage(ALLOCATED_DATABASE_STORAGE_NUMBER)
                .multiAz(false)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
                .databaseName(keeperDatabaseName.getValueAsString())
                .backupRetention(Duration.days(0))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        postgres.getConnections().allowFromAnyIpv4(Port.tcp(DATABASE_PORT), "Allow connections to the database");

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
                                        "POSTGRES_HOST", postgres.getDbInstanceEndpointAddress(),
                                        "POSTGRES_PORT", postgres.getDbInstanceEndpointPort(),
                                        "POSTGRES_DATABASE", keeperDatabaseName.getValueAsString()
                                ))
                                .secrets(Map.of(
                                        "POSTGRES_USER", Secret.fromSecretsManager(keeperDatabaseUserSecret, "username"),
                                        "POSTGRES_PASSWORD", Secret.fromSecretsManager(keeperDatabaseUserSecret, "password")
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
