FROM nginx:stable-alpine

COPY ./compose/gowherene.conf.template /etc/nginx/conf.d/
COPY ./resources/public /usr/share/nginx/html

RUN rm -rf /usr/share/nginx/html/js
COPY ./cljs-prod-js /usr/share/nginx/html/js