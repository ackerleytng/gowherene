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

You will need at least version 1.10.1.727 of the Clojure CLI installed.

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

### Starting the backend

In emacs, do `cider-jack-in`, then at the `user>` prompt, do

```
user> (def server (start-gowherene))
... elided ...
#'user/server
```

And then check that the backend is up with `curl`:

```
curl -X GET 'http://localhost:3000/parse/?url=http://thesmartlocal.com/read/restaurants-with-no-gst' | jq
```

### Stopping the backend

```
user> (.stop server)
```

### Starting the figwheel-main dev server (frontend)

Do `C-c M-J` and when prompted, enter `figwheel-main`. When asked for build, enter `dev`

A browser tab should open, pointing to `http://localhost:9500`.

## Production/deployment flow

To build for production

```
make
```

This should build both the backend and frontend.

### Testing out the backend

```
GOWHERENE_DEBUG=1 java -cp target/gowherene.jar clojure.main -m gowherene.core
```

The above should start a server on port 3000.

To query it,

```
curl -vv -X GET 'http://localhost:3000/parse/?url=http://thesmartlocal.com/read/restaurants-with-no-gst' | jq
```

### Deploying backend

Upload backend to server with

```
scp target/gowherene.jar alwaysdata:
```

### Deploying frontend

Go to netlify, drag and drop `target/dist` to upload.

## Backend environment variables

In `gowherene`, I am expecting the following environment variables to be in place.

| key                 | value                                                          |
|---------------------|----------------------------------------------------------------|
| `:google-api-token` | API token for Google Maps geocoding API                        |
| `:port`             | The port to run the server at (defaults to 3000)               |
| `:gowherene-debug`  | Set to `true` to prevent auto redirecting to HTTPS on port 443 |

For development, I use a `.lein-env` file in the project directory, which looks like

```
{:google-api-token "xxx"
 :gowherene-debug true}
```

> `.lein-env` works even without using leiningen because `environ` looks for that file

## License

Copyright © 2022 ackerleytng
