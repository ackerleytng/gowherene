all: backend frontend

frontend: target/dist/js/main.js target/dist/css/style.css target/dist/img/gowherene.svg target/dist/img/spinner.svg target/dist/index.html

target/dist/js/main.js:
	clj -M:frontend

target/dist/%: resources/public/%
	mkdir -p $$(dirname $@)
	cp $< $@

backend: target/gowherene.jar

target/gowherene.jar:
	clj -T:build uber

clean:
	rm -rf target

.PHONY: all frontend backend clean
