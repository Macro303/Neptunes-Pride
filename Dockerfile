FROM openjdk:8
ADD ./ /
CMD ["gradlew", "clean", "run"]