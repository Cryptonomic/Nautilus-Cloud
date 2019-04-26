
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


--keyid	numeric, primary key	sequence auto-increment pk
--key	text, not null	32-char API key
--resourceid	numeric	FK reference to resources table
--userid	numeric	FK reference to users table
--tierid	numeric	FK reference to tiers table
--dateissued	timestamp	date from which the key is active
--datesuspended	timestamp	a means of terminating a key

--tierid	numeric, primary key	sequence auto-increment pk
--tier	text, not null	tier name
--description	text, not null	long-form service description
--monthlyhits	numeric, not null	cumulative number of requests per calendar month - static window
--dailyhits	numeric, not null	24-hour window request allowance - sliding window
--effectivedate	timestamp, not null	validity period start
--enddate	timestamp, not null	validity period end



--userid	numeric, primary key	sequence auto-increment pk
--username	text, not null	login name
--useremail	text, not null	email
--userrole	text, not null, default('user')	role, probably enum, other values might be acctadmin (corp customers may want control over their sub-accounts), infraadmin (require 2FA)
--registrationdate, not null	timestamp	validity period start
--accountsource	text, nullable	might be enum, values could be web, campaign id, "internal", "manual" (when we ourselves might make accounts for people)
--accountdescription	text, nullable	to go with account source, something we may enter



--resourceid	numeric, primary key	sequence auto-increment pk
--resourcename	text, not null
--description	text, not null
--platform	text, not null	'Conseil', 'Tezos', etc
--network	text, not null	'prod', 'mainnet', etc

