package com.kabaev.shop.service.keeper.infrastructure;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

public class DataStack extends Stack {

    private final IDatabaseInstance postgres;
    private final ISecret keeperDatabaseUserSecret;
    private final CfnParameter keeperDatabaseName;

    public DataStack(
            final Construct scope,
            final String id,
            final StackProps props,
            final NetworkStack networkStack) {
        super(scope, id, props);

        final int ALLOCATED_DATABASE_STORAGE_NUMBER = 20;
        final int DATABASE_PORT = 5432;

        keeperDatabaseName = CfnParameter.Builder.create(this, "keeperDatabaseName")
                .description("The name of the Keeper Service Database keeping information about all products.")
                .defaultValue("keeperdb")
                .build();

        var keeperDatabaseUserName = CfnParameter.Builder.create(this, "keeperDatabaseUserName")
                .description("The name of the Keeper Service Database user.")
                .defaultValue("managerdb")
                .build();

        keeperDatabaseUserSecret = DatabaseSecret.Builder.create(this, "keeperDatabaseUserSecret")
                .username(keeperDatabaseUserName.getValueAsString())
                .build();

        Vpc vpc = networkStack.getVpc();

        postgres = DatabaseInstance.Builder.create(this, "postgres")
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
    }

    public IDatabaseInstance getPostgres() {
        return postgres;
    }

    public ISecret getKeeperDatabaseUserSecret() {
        return keeperDatabaseUserSecret;
    }

    public CfnParameter getKeeperDatabaseName() {
        return keeperDatabaseName;
    }

}
