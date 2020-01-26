build: build-frontend build-backend
	docker-compose build

build-frontend: cljs-prod-js/main.js

cljs-prod-js/main.js:
	lein fig:prod

build-backend: target/gowherene-0.1.0-SNAPSHOT-standalone.jar

target/gowherene-0.1.0-SNAPSHOT-standalone.jar:
	lein uberjar

clean:
	rm -rf target cljs-prod-js

.PHONY: build build-frontend build-backend clean
