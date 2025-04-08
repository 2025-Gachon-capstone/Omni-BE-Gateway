# builder image
FROM amazoncorretto:17-al2-jdk AS builder

RUN mkdir /Omni-BE-Gateway
WORKDIR /Omni-BE-Gateway

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar

# runtime image
FROM amazoncorretto:17.0.12-al2

ENV TZ=Asia/Seoul
ENV PROFILE=${PROFILE}

RUN mkdir /Omni-BE-Gateway
WORKDIR /Omni-BE-Gateway

COPY --from=builder /Omni-BE-Gateway/build/libs/Omni-BE-Gateway-* /Omni-BE-Gateway/app.jar

CMD ["sh", "-c", " \
    java -Dspring.profiles.active=${PROFILE} \
         -jar /Omni-BE-Gateway/app.jar"]
