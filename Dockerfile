FROM openjdk:11-jre-slim
COPY rotina-pagamento-resource/target/rotina-pagamento-resource.jar rotina-pagamento.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=container","-jar","rotina-pagamento.jar"]