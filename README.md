# Nautilus-Cloud
Cloud interface for Nautilus infrastructure

# How to run
Fill in configuration in `src/main/resources/reference.conf` for database and for github and run an app:
```
sbt run
```

You can provide your own configuration file called `your-username-dev.conf` as well. Place it to `src/main/resources/` 
and consider adding it to you local git ignore file. Then you can run Nautilus-Cloud using following command:
```
sbt run -Dconfig.resource=your-username-dev.conf
```

# Using
* swagger: http://localhost:1234/docs
* login-panel: http://localhost:1234/site