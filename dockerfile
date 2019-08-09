FROM openjdk:8-jre-alpine
WORKDIR /nc
COPY ./target/scala-2.12/nautilus-cloud*.jar /nc/
EXPOSE 1234
CMD java -Xms512m -Xmx14g -Dconfig.file=nc.conf -cp ./*.jar tech.cryptonomic.nautilus.cloud.NautilusCloud
