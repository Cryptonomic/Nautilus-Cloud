create TABLE environments(
    name varchar(11) PRIMARY KEY
);
create TABLE resources(
    resourceid serial PRIMARY KEY,
    resourcename text NOT NULL,
    description text NOT NULL,
    platform text NOT NULL,
    network text NOT NULL,
    environment varchar(11) NOT NULL,
    CONSTRAINT environment_fk FOREIGN KEY (environment)
      REFERENCES environments (name) MATCH SIMPLE
      ON update NO ACTION ON delete NO ACTION
);
create TABLE tiers(
    tierid serial PRIMARY KEY,
    tier text NOT NULL,
    subtier text NOT NULL,
    UNIQUE(tier, subtier)
);
create TABLE tiers_configuration(
    tier text NOT NULL,
    subtier text NOT NULL,
    description text NOT NULL,
    monthlyhits integer NOT NULL,
    dailyhits integer NOT NULL,
    maxresultsetsize integer NOT NULL,
    startdate timestamp NOT NULL,
    FOREIGN KEY (tier, subtier) REFERENCES tiers (tier, subtier)
);
create TABLE users(
    userid serial PRIMARY KEY,
    useremail text NOT NULL UNIQUE,
    userrole text NOT NULL DEFAULT 'user',
    registrationdate timestamp NOT NULL,
    accountsource text NOT NULL,
    registrationIp varchar(50),
    tosAccepted boolean NOT NULL,
    newsletterAccepted boolean NOT NULL,
    newsletterAcceptedDate timestamp with time zone,
    accountdescription text,
    deleteddate timestamp with time zone
);
create TABLE api_keys(
    keyid serial PRIMARY KEY,
    key text NOT NULL,
    environment varchar(11) NOT NULL,
    userid integer NOT NULL,
    dateissued timestamp with time zone,
    datesuspended timestamp with time zone,
    UNIQUE(key),
    CONSTRAINT environment_fk FOREIGN KEY (environment)
      REFERENCES environments (name) MATCH SIMPLE
      ON update NO ACTION ON delete NO ACTION,
    CONSTRAINT userid_fk FOREIGN KEY (userid)
      REFERENCES users (userid) MATCH SIMPLE
      ON update NO ACTION ON delete NO ACTION
);
create TABLE usage_left(
    key text NOT NULL,
    daily integer NOT NULL,
    monthly integer NOT NULL,
    CONSTRAINT keyid_fk FOREIGN KEY (key)
      REFERENCES api_keys (key) MATCH SIMPLE
      ON update NO ACTION ON delete NO ACTION
);
create TABLE metering_statistics(
    id serial PRIMARY KEY,
    userid integer NOT NULL,
    environment text NOT NULL,
    hits integer NOT NULL,
    period_start timestamp with time zone NOT NULL,
    period_end timestamp with time zone NOT NULL,
    CONSTRAINT userid_fk FOREIGN KEY (userid)
      REFERENCES users (userid) MATCH SIMPLE
      ON update NO ACTION ON delete NO ACTION
);

CREATE UNIQUE INDEX metering_statistics_idx ON metering_statistics(userid, period_start, period_end);

create TABLE user_history(
    time timestamp with time zone NOT NULL,
    userid integer NOT NULL,
    performed_by integer,
    ip varchar(50),
    action text NOT NULL
);

insert into environments (name) values('dev');
insert into environments (name) values('prod');

--- Static resources described in #30
insert into resources (resourcename, description, platform, network, environment) values('Tezos Alphanet Conseil Dev', 'Conseil alphanet development environment', 'tezos', 'alphanet', 'dev');
insert into resources (resourcename, description, platform, network, environment) values('Tezos Mainnet Conseil Dev', 'Conseil mainnet development environment', 'tezos', 'mainnet', 'dev');
insert into resources (resourcename, description, platform, network, environment) values('Tezos Alphanet Conseil Prod', 'Conseil alphanet production environment', 'tezos', 'alphanet', 'prod');
insert into resources (resourcename, description, platform, network, environment) values('Tezos Mainnet Conseil Prod', 'Conseil mainnet production environment', 'tezos', 'mainnet', 'prod');

insert into tiers (tier, subtier) values('shared', 'free');
insert into tiers_configuration (tier, subtier, description, monthlyhits, dailyhits, maxresultsetsize, startdate) values('shared', 'free', 'free tier', 1000, 100, 10, current_timestamp);
