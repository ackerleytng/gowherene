# playout

FIXME

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```
$ lein ring server
```

## Deploying on heroku

Install the heroku toolbelt, then

```
$ heroku plugins:install heroku-cli-deploy
$ heroku create mappout --no-remote
$ heroku deploy:jar target/playout-0.1.0-SNAPSHOT-standalone.jar --app mappout
```

To see logs,

```
$ heroku logs --tail --app mappout
```

## License

Copyright © 2017 ackerleytng
