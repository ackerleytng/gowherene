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

| key                | value                                                                                                    |
|--------------------+----------------------------------------------------------------------------------------------------------|
| `:postal-code-loc` | The loc (as in point in hickory) where the postal code was found, together with some address information |
| `:header-loc`      | The loc of the heuristically-determined header for this `:postal-code-loc`                               |
| `:place`           | The name of this place                                                                                   |
| `:address`         | The address of this place                                                                                |
| `:latlng`          | The latitude and longitude of this place                                                                 |

## Running

To start a web server for the application, run:

```
$ lein ring server
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
