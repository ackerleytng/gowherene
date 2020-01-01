build: build-frontend build-backend
	docker-compose build

build-frontend:
	lein fig:prod

build-backend:
	lein uberjar

clean:
	rm -rf target cljs-prod-js

.PHONY: build build-frontend build-backend clean
