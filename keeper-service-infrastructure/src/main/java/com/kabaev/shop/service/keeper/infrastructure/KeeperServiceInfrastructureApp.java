package com.kabaev.shop.service.keeper.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class KeeperServiceInfrastructureApp {

    public static void main(final String[] args) {
        App app = new App();

        Environment environment = Environment.builder()
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .build();

        var networkStack  = new NetworkStack(
                app,
                "networkStack",
                StackProps.builder()
                        .env(environment)
                        .build()
        );

        var dataStack = new DataStack(
                app,
                "dataStack",
                StackProps.builder()
                        .env(environment)
                        .build(),
                networkStack
        );

        new ServiceStack(
                app,
                "serviceStack",
                StackProps.builder()
                        .env(environment)
                        .build(),
                networkStack,
                dataStack
        );

        app.synth();
    }

}

