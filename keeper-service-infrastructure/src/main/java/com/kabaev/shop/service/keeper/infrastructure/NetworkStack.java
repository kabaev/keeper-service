package com.kabaev.shop.service.keeper.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class NetworkStack extends Stack {

    private final Vpc vpc;

    public NetworkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final int AVAILABILITY_ZONE_NUMBER = 3;

        vpc = Vpc.Builder.create(this, "VPC")
                .maxAzs(AVAILABILITY_ZONE_NUMBER)
                .subnetConfiguration(Vpc.DEFAULT_SUBNETS_NO_NAT)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }

}
