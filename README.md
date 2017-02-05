Open Source Lifelogging
=======================

Backend server for the open-lifelogging android app and webclient

## Deployment

 - `docker build -t open-lifelogging .`
 - `docker run --name open-lifelogging -p 9000:9000 -d open-lifelogging` 
 - adjust Environment: PSQL_URL; PSQL_USER; PSQL_PASSWORD (full list in src/main/resources/application.conf)

## Features

- [x] Wifi logging
- [x] Location logging
- [x] Arbitrary json logging
- [x] Registration/Login
- [x] LastFm connection
- [ ] Logout
- [ ] Filter by date range
- [ ] Owncloud connection
- [ ] log server events

## Known Issues

 - Tests not running
 - LastFm ignore already fetched
 - LastFm pagination missing

## Copyright

Based on Akka Slick REST service template: Copyright (C) 2015 Arthur Kushka.

Copyright (C) 2016 Philipp Piwowarsky.

Distributed under the MIT License.
