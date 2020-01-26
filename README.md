# gowherene

gowherene is a webapp to help Singaporeans plot food recommendations, such as
thesmartlocal's http://thesmartlocal.com/read/cheap-food-orchard, on a map.

These popular blogs do not have a map view for their recommendations, so I built
gowherene to help visualise the geographical locations of the recommendations
for better decision making.

Some carefully-tested recommendations pages are

+ http://thesmartlocal.com/read/restaurants-with-no-gst
+ http://thesmartlocal.com/read/singapore-cafes-with-no-gst
+ https://sethlui.com/best-burgers-singapore/
+ https://www.sassymamasg.com/the-ultimate-guide-to-local-breakfast-in-singapore/
+ https://www.misstamchiak.com/local-breakfast-east-singapore/

gowherene can also plot addresses, not just recommendations! Try:

+ http://international.tiffany.com/jewelry-stores/store-list/singapore

## Prerequisites

You will need [Leiningen][] 2.5.3 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## `data`

While parsing a page to find address information,
the code often refers to `data` or `address-info`, a map of:

| key                 | value                                                                                                      |
| ------------------- | ---------------------------------------------------------------------------------------------------------- |
| `:postal-code-loc`  | The loc (as in point in hickory) where the postal code was found, together with some address information   |
| `:header-loc`       | The loc of the heuristically-determined header for this `:postal-code-loc`                                 |
| `:place`            | The name of this place                                                                                     |
| `:address`          | The address of this place                                                                                  |
| `:latlng`           | The latitude and longitude of this place                                                                   |

## Developing

### Install and start `caddy`

Download caddy and put it on your `PATH`: https://caddyserver.com/download/linux/amd64?license=personal&telemetry=off

Start caddy: (it will read `Caddyfile`)

```
$ caddy
Activating privacy features... done.

Serving HTTP on port 2015
http://localhost:2015/
http://localhost:2015/parse

< ... elided ... >
```

### Starting the backend

In emacs, do `cider-jack-in`, then at the `user>` prompt, do

```
user> (start)
... elided ...
{:started ["#'dev/gowherene-app"]}
```

And then check that the backend is up with `curl`:

```
curl -X GET 'http://localhost:3000/?url=http://thesmartlocal.com/read/restaurants-with-no-gst' | jq
```

### Starting the figwheel-main dev server (frontend)

Do `C-c M-J` and when prompted, enter `figwheel-main`. When asked for build, enter `dev`

A browser tab should open, pointing to `http://localhost:9500`.

We'll be using caddy to proxy to that instead, so surf to `http://localhost:2015` (caddy) to begin.

#### figwheel-main dev server on cli

You can also start the `figwheel-main` dev server on the command line with

```
lein fig:dev
```

## Staging

To check production cljs build, first do a production cljs build:

```
lein fig:prod
```

This should compile cljs and output to `cljs-prod-js/main.js`

### Starting the backend

Either start the backend through emacs (same steps as development), or run the jar:

```
java -jar target/gowherene-0.1.0-SNAPSHOT-standalone.jar
```

### Start `caddy`

```
caddy -conf Caddyfile-staging
```

Surf to `http://localhost:2015` to check that it works.

## Production/deployment flow

Build docker images

```
make images
```

Upload them to server (I'm using aws with a cloudflare CDN)

```
scp nginx.tar backend.tar aws:
```

On the aws instance, update the `docker-compose.yml` with `git pull`, then `docker-compose restart`

## Backend environment variables

In `gowherene`, I am expecting the following environment variables to be in place.

| key                 | value                                            |
| ---                 | ---                                              |
| `:google-api-token` | API token for Google Maps geocoding API          |
| `:port`             | The port to run the server at (defaults to 3000) |

For development, I use a `.lein-env` file in the project directory, which looks like

```
{:google-api-token "xxx"}
```

In production, gowherene will preferentially use the docker secret `/run/secrets/google-api-token`, if the file exists, falling back to the environment variable `GOOGLE_API_TOKEN`.

## License

Copyright © 2020 ackerleytng
