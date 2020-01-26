images: backend.tar nginx.tar

backend.tar: build
	docker save -o backend.tar gowherene_backend

nginx.tar: build
	docker save -o nginx.tar gowherene_nginx

build: build-backend build-frontend
	docker-compose build

build-frontend: cljs-prod-js/main.js

cljs-prod-js/main.js:
	lein fig:prod

build-backend: target/gowherene-0.1.0-SNAPSHOT-standalone.jar

target/gowherene-0.1.0-SNAPSHOT.jar target/gowherene-0.1.0-SNAPSHOT-standalone.jar:
	lein uberjar

clean:
	rm -rf target cljs-prod-js *.tar

.PHONY: images build build-frontend build-backend clean
