FROM openjdk:21
ADD target/docker-GenDailyNotifMohtm.jar docker-GenDailyNotifMohtm.jar
EXPOSE 8070
ENTRYPOINT ["java","-Duser.timezone=\"Africa/Cairo\"","-jar","docker-GenDailyNotifMohtm.jar"]