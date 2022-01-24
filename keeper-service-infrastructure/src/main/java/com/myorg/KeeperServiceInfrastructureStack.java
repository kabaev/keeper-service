package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;
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

        CfnParameter postgresDatabaseName = CfnParameter.Builder.create(this, "postgresDatabaseName")
                .defaultValue("keeperdb")
                .build();

        CfnParameter postgresUserName = CfnParameter.Builder.create(this, "postgresUserName")
                .defaultValue("userdb")
                .build();

        var postgresUserSecret = DatabaseSecret.Builder.create(this, "PostgresCredentials")
                .username(postgresUserName.getValueAsString())
                .build();

        Vpc vpc = Vpc.Builder.create(this, "VPC")
                .maxAzs(3)
                .subnetConfiguration(Vpc.DEFAULT_SUBNETS_NO_NAT)
                .build();

        var postgres = DatabaseInstance.Builder.create(this, "Postgres")
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
                .engine(
                        DatabaseInstanceEngine.postgres(
                                PostgresInstanceEngineProps.builder()
                                        .version(PostgresEngineVersion.VER_13_4)
                                        .build()
                        )
                )
                .credentials(Credentials.fromSecret(postgresUserSecret))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .databaseName(postgresDatabaseName.getValueAsString())
                .backupRetention(Duration.days(0))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        postgres.getConnections().allowFromAnyIpv4(Port.tcp(5432), "Allow connections to the database");

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
