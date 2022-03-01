#!/bin/sh

export AWS_ACCESS_KEY_ID=CHANGE_IT
export AWS_SECRET_ACCESS_KEY=CHANGE_IT

alias awsl='aws --endpoint-url=http://localstack:4566 --region us-east-2'

awsl s3api create-bucket --bucket keeper
awsl sns create-topic --name product-updates
