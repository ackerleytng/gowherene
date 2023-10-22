# syntax=docker/dockerfile:1

FROM clojure:temurin-17-tools-deps-bullseye AS builder
WORKDIR /tmp/
COPY . .
ENV GOOGLE_API_TOKEN=dummy-token-for-build
RUN clj -T:build uber

FROM gcr.io/distroless/java17-debian11
COPY --from=builder /tmp/target/gowherene.jar /app/gowherene.jar
WORKDIR /app
CMD ["gowherene.jar"]
