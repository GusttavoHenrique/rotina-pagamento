FROM openjdk:8-jdk-alpine
COPY rotina-pagamento-resource/target/rotina-pagamento-resource.jar rotina-pagamento.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xmx128m", "-jar", "/rotina-pagamento.jar"]