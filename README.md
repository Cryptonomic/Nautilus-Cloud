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

# Usage
* swagger: http://localhost:1234/docs
* login-panel: http://localhost:1234/site