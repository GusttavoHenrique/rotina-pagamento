FROM piegsaj/oracle-jre
COPY rotina-pagamento-resource/target/rotina-pagamento-resource.jar payment-routine.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xmx128m", "-jar", "/payment-routine.jar"]