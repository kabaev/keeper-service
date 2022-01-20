package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class KeeperServiceInfrastructureApp {

    public static void main(final String[] args) {
        App app = new App();

        new KeeperServiceInfrastructureStack(app, "KeeperServiceInfrastructureStack", StackProps.builder()
                .build());

        app.synth();
    }

}

