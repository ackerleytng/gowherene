# syntax=docker/dockerfile:1

FROM clojure:temurin-21-tools-deps-bookworm AS builder
WORKDIR /tmp/
COPY . .
RUN clj -T:build uber

FROM gcr.io/distroless/java21-debian12
COPY --from=builder /tmp/target/gowherene.jar /app/gowherene.jar
WORKDIR /app
CMD ["gowherene.jar"]
