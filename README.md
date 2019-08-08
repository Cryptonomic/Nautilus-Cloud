[![Build Status](https://travis-ci.com/Cryptonomic/Nautilus-Cloud.svg?token=8NXhD5Q5yeNRbmzW1fVc&branch=master)](https://travis-ci.com/Cryptonomic/Nautilus-Cloud) [![Coverage Status](https://coveralls.io/repos/github/Cryptonomic/Nautilus-Cloud/badge.svg?t=eBIu8J)](https://coveralls.io/github/Cryptonomic/Nautilus-Cloud)

# Nautilus-Cloud
Cloud interface for Nautilus infrastructure

# How to run
Before you begin you need to create an `Oauth App` on Github (https://github.com/settings/developers) to be able to 
log in. `Authorization callback URL` needs to point to your app with the following path: `/github-callback`. For 
your local environment the whole uri will be: `http://localhost:1234/github-callback`.  

Fill in configuration in `src/main/resources/reference.conf` for database and for github (`Client ID` and `Client 
Secret` taken from `Oauth App` you've created) and run an app:

```
sbt run
```

You can provide your own configuration file called `your-username-dev.conf` as well. Place it to `src/main/resources/` 
and consider adding it to your local `.gitignore file` to keep it away from being committed by accident. 

Then you can run Nautilus-Cloud using following command:

```
sbt run -Dconfig.resource=your-username-dev.conf
```

#Docker
It is possible to run a dockerized version of Nautilus Cloud. Assuming you have configured reference.conf correctly
as described above (take note of the doobie.host config), follow these steps:

1. Run `sbt assembly`. This will produce an uber jar that can then be picked up by docker.
2. Run `docker-compose up`. This will start Nautilus Cloud and its associated Postgres DB.
3. Open `localhost:1234` in your browser. You should be greeted by the basic landing page.

# Usage
* swagger: http://localhost:1234/docs
* login-panel: http://localhost:1234/site