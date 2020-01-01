FROM abiosoft/caddy:no-stats

COPY ./compose/Caddyfile /etc/Caddyfile

COPY ./resources/public/ /srv/

RUN rm -rf /srv/js/
COPY ./cljs-prod-js/ /srv/js/