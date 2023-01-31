FROM vertx/vertx4
ENV VERTICLE_NAME org.example.App.class
ENV VERTICLE_HOME /home/ideaProjects/untitled1/classes/org/example
ENV VERTICLE_FILE target/-1.0-SNAPSHOT.jar
RUN mkdir -p /home/ideaProjects/untitled1/
COPY * /home/ideaProjects/untitled1/
WORKDIR $VERTICLE_HOME
EXPOSE 8080
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]