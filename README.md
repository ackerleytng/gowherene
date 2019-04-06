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

You will need [Leiningen][] 2.0.0 or above installed.

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

## Dev Quickstart Guide

In emacs, do `cider-jack-in`, then at the `user>` prompt, do

```
user> (start)
... elided ...
{:started ["#'dev/gowherene-app"]}
```

And then in another terminal window, do

```
curl -X GET 'http://localhost:3000/parse?url=http://thesmartlocal.com/read/restaurants-with-no-gst' | jq
```

## Environment variables

In `gowherene`, I am expecting the following environment variables to be in place.

| key                 | value                                   |
| ---                 | ---                                     |
| `:google-api-token` | API token for Google Maps geocoding API |
| `:database-url`     | Database url for logging accesses       |
| `:mongodb-uri`      | MongoDB uri for logging requests        |


For development, I use a `.lein-env` file in the project directory, which looks like

```
{:google-api-token "xxx"
 :database-url     "xxx"
 :mongodb-uri      "xxx"}
```

## Viewing/erasing logs

There are a few lein aliases defined for viewing/erasing the request and access logs now. Development logs can be viewed with

```
$ lein accesses-show
$ lein requests-show
```

To show logs stored with heroku, use

```
$ DATABASE_URL='<copy from heroku env>' lein accesses-show
$ MONGODB_URI='<copy from heroku env>' lein requests-show
```

For the development MongoDB, I'm using MongoDB atlas, which only accepts connections from a whitelist of IPs.
You'll need to allow your (dynamic) IP through before MongoDB atlas will permit access.

For the development accesses log, I'm using postgresql, hosted at ElephantSQL.
I believe ElephantSQL does not have this system of whitelists.

To erase the logs, do

```
$ lein accesses-cleanup
$ lein requests-cleanup
```

The free databases have limits, hence the need to erase periodically.

## Running

To start a web server for the application, run:

```
$ lein ring server
```

## Developing

Start figwheel with

```
$ rlwrap lein figwheel
```

Start the server with

```
$ lein ring server-headless
```

## Testing

To write test cases, update the test case, then evaluate it (`C-M-x`), then re-run test cases (`C-c C-t C-n`)

## Deploying on heroku

Package for deployment with

```
$ lein clean
$ lein ring uberjar
```

Install the heroku toolbelt, then

```
$ heroku plugins:install heroku-cli-deploy
$ heroku create gowherene --no-remote
$ heroku deploy:jar target/gowherene-0.1.0-SNAPSHOT-standalone.jar --app gowherene
```

To see logs,

```
$ heroku logs --tail --app gowherene
```

## License

Copyright © 2018 ackerleytng
