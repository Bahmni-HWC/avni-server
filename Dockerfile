FROM amazoncorretto:8
COPY avni-server-api/build/libs/avni-server-0.0.1-SNAPSHOT.jar /opt/openchs/avni-server.jar
COPY avni-webapp-build/ /opt/openchs/static/
CMD java $OPENCHS_SERVER_OPTS -jar /opt/openchs/avni-server.jar