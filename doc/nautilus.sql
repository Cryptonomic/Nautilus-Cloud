
CREATE TABLE resources(
    resourceid serial PRIMARY KEY,
    resourcename text NOT NULL,
    description text NOT NULL,
    platform text NOT NULL,
    network text NOT NULL
);
CREATE TABLE tiers(
    tierid serial PRIMARY KEY,
    tier text NOT NULL,
    subtier text NOT NULL,
    UNIQUE(tier, subtier)
);
CREATE TABLE tiers_configuration(
    tier text NOT NULL,
    subtier text NOT NULL,
    description text NOT NULL,
    monthlyhits integer NOT NULL,
    dailyhits integer NOT NULL,
    maxresultsetsize integer NOT NULL,
    enddate timestamp,
    FOREIGN KEY (tier, subtier) REFERENCES tiers (tier, subtier)
);
CREATE TABLE users(
    userid serial PRIMARY KEY,
    useremail text NOT NULL UNIQUE,
    userrole text NOT NULL DEFAULT 'user',
    registrationdate timestamp NOT NULL,
    accountsource text NOT NULL,
    accountdescription text
);
CREATE TABLE api_keys(
    keyid serial PRIMARY KEY,
    key text NOT NULL,
    resourceid integer NOT NULL,
    userid integer NOT NULL,
    tierid integer NOT NULL,
    dateissued timestamp with time zone,
    datesuspended timestamp with time zone,
    UNIQUE(key),
    CONSTRAINT resourceid_fk FOREIGN KEY (resourceid)
      REFERENCES resources (resourceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT userid_fk FOREIGN KEY (userid)
      REFERENCES users (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT tierid_fk FOREIGN KEY (tierid)
      REFERENCES tiers (tierid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE usage_left(
    key text NOT NULL,
    daily integer NOT NULL,
    monthly integer NOT NULL,
    CONSTRAINT keyid_fk FOREIGN KEY (key)
      REFERENCES api_keys (key) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)

