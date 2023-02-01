FROM openjdk:17-slim
ARG dbpass
ARG api_key
ENV api_key $api_key
ENV dbpass $dbpass
ENV VERTICLE_HOME /home/ideaProjects/untitled1/
RUN mkdir -p /home/ideaProjects/untitled1/
COPY * /home/ideaProjects/untitled1/
WORKDIR $VERTICLE_HOME
EXPOSE 8080
CMD ["java","-jar","untitled1-1.0-SNAPSHOT-fat.jar"]