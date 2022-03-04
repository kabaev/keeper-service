#!/bin/sh
export AWS_ACCESS_KEY_ID=CHANGE_IT
export AWS_SECRET_ACCESS_KEY=CHANGE_IT

alias awsl='aws --endpoint-url=http://localstack:4566 --region us-east-2'

awsl s3api create-bucket --bucket keeper-bucket
awsl sns create-topic --name product-updates
awsl sqs create-queue --queue-name product-queue.fifo --attributes FifoQueue=true
awsl sns subscribe --topic-arn arn:aws:sns:us-east-2:000000000000:product-updates \
                   --protocol sqs \
                   --notification-endpoint http://localhost:4566/000000000000/product-queue.fifo