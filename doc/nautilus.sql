
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
    description text NOT NULL,
    monthlyhits integer NOT NULL,
    dailyhits integer NOT NULL,
    effectivedate timestamp NOT NULL,
    enddate timestamp NOT NULL
);
CREATE TABLE users(
    userid serial PRIMARY KEY,
    username text NOT NULL,
    useremail text NOT NULL,
    userrole text NOT NULL DEFAULT 'user',
    registrationdate timestamp NOT NULL,
    accountsource text,
    accountdescription text
);
CREATE TABLE api_keys(
    keyid serial PRIMARY KEY,
    key text NOT NULL,
    resourceid integer NOT NULL,
    userid integer NOT NULL,
    tierid integer NOT NULL,
    dateissued timestamp,
    datesuspended timestamp,
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
    keyid integer NOT NULL,
    daily integer NOT NULL,
    monthly integer NOT NULL,
    CONSTRAINT keyid_fk FOREIGN KEY (keyid)
      REFERENCES api_keys (keyid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)

