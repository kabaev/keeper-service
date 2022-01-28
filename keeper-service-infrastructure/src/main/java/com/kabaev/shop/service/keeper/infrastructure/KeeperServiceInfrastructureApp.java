package com.kabaev.shop.service.keeper.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class KeeperServiceInfrastructureApp {

    public static void main(final String[] args) {
        App app = new App();

        var networkStack  = new NetworkStack(
                app,
                "networkStack",
                StackProps.builder().build()
        );

        var dataStack = new DataStack(
                app,
                "dataStack",
                StackProps.builder().build(),
                networkStack
        );

        new ServiceStack(
                app,
                "serviceStack",
                StackProps.builder().build(),
                networkStack,
                dataStack
        );

        app.synth();
    }

}

