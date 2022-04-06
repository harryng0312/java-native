CREATE TABLE user_
(
    id_                     varchar2(36)  not null,
    created_date            datetime      not null,
    modified_date           datetime      not null,
    status                  varchar2(36)  not null,
    screenname              varchar2(36)  not null,
    username                varchar2(36)  not null,
    password_               varchar2(100) not null,
    dob                     date          not null,
    passwd_encrypted_method varchar2(50)  not null default '',
    CONSTRAINT user_pk PRIMARY KEY (id_)
);

CREATE UNIQUE INDEX user_IX1 ON user_ (id_);
CREATE INDEX user_IX2 ON user_ (username);

INSERT INTO USER_(ID_, CREATED_DATE, MODIFIED_DATE, STATUS, SCREENNAME,
                  USERNAME, PASSWORD_, DOB, PASSWD_ENCRYPTED_METHOD)
VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '1', 'screen name 1',
        'username 1', 'passwd1', CURRENT_DATE, 'plain');