FROM openjdk:17-alpine

RUN apk --no-cache add aws-cli

WORKDIR /tmp

ADD init-aws-resources.sh ./
RUN chmod +x init-aws-resources.sh

ENTRYPOINT ["./init-aws-resources.sh"]
