CREATE TABLE authorization_consent
(
    registered_client_id VARCHAR(255) NOT NULL,
    principal_name       VARCHAR(255) NOT NULL,
    authorities          VARCHAR(1000),
    CONSTRAINT pk_authorizationconsent PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE client
(
    id                            VARCHAR(255) NOT NULL,
    client_id                     VARCHAR(255),
    client_id_issued_at           TIMESTAMP WITHOUT TIME ZONE,
    client_secret                 VARCHAR(255),
    client_secret_expires_at      TIMESTAMP WITHOUT TIME ZONE,
    client_name                   VARCHAR(255),
    client_authentication_methods VARCHAR(1000),
    authorization_grant_types     VARCHAR(1000),
    redirect_uris                 VARCHAR(1000),
    post_logout_redirect_uris     VARCHAR(1000),
    scopes                        VARCHAR(1000),
    client_settings               VARCHAR(2000),
    token_settings                VARCHAR(2000),
    CONSTRAINT pk_client PRIMARY KEY (id)
);

CREATE TABLE user_authorization
(
    id                            VARCHAR(255) NOT NULL,
    registered_client_id          VARCHAR(255),
    principal_name                VARCHAR(255),
    authorization_grant_type      VARCHAR(255),
    authorized_scopes             VARCHAR(255),
    attributes                    VARCHAR(255),
    state                         VARCHAR(500),
    authorization_code_value      VARCHAR(255),
    authorization_code_issued_at  TIMESTAMP WITHOUT TIME ZONE,
    authorization_code_expires_at TIMESTAMP WITHOUT TIME ZONE,
    authorization_code_metadata   VARCHAR(255),
    access_token_value            VARCHAR(255),
    access_token_issued_at        TIMESTAMP WITHOUT TIME ZONE,
    access_token_expires_at       TIMESTAMP WITHOUT TIME ZONE,
    access_token_metadata         VARCHAR(255),
    access_token_type             VARCHAR(255),
    access_token_scopes           VARCHAR(255),
    refresh_token_value           VARCHAR(255),
    refresh_token_issued_at       TIMESTAMP WITHOUT TIME ZONE,
    refresh_token_expires_at      TIMESTAMP WITHOUT TIME ZONE,
    refresh_token_metadata        VARCHAR(255),
    oidc_id_token_value           VARCHAR(255),
    oidc_id_token_issued_at       TIMESTAMP WITHOUT TIME ZONE,
    oidc_id_token_expires_at      TIMESTAMP WITHOUT TIME ZONE,
    oidc_id_token_metadata        VARCHAR(255),
    oidc_id_token_claims          VARCHAR(255),
    user_code_value               VARCHAR(255),
    user_code_issued_at           TIMESTAMP WITHOUT TIME ZONE,
    user_code_expires_at          TIMESTAMP WITHOUT TIME ZONE,
    user_code_metadata            VARCHAR(255),
    device_code_value             VARCHAR(255),
    device_code_issued_at         TIMESTAMP WITHOUT TIME ZONE,
    device_code_expires_at        TIMESTAMP WITHOUT TIME ZONE,
    device_code_metadata          VARCHAR(255),
    CONSTRAINT pk_user_authorization PRIMARY KEY (id)
);