DROP TABLE account if EXISTS ;
DROP TABLE client_details if EXISTS ;

create table account ( ACCOUNT_NAME varchar(255) not null,
                      PASSWORD varchar(255 ) not null,
                      ID serial,
                      ENABLED bool default true)  ;

create table client_details(
  CLIENT_ID VARCHAR (255) not null unique ,
  CLIENT_SECRET VARCHAR (255) not null   ,
  RESOURCE_IDS VARCHAR (255) null ,
  SCOPES VARCHAR (255) null ,
  GRANT_TYPES VARCHAR (255) null ,
  AUTHORITIES VARCHAR (255) null
);