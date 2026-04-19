CREATE TABLE IF NOT EXISTS users
(
    id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email text NOT NULL,
    name  text NOT NULL,

    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items
(
    item_id     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        text    NOT NULL,
    description text    NOT NULL,
    available   boolean NOT NULL DEFAULT true,
    owner_id    bigint  NOT NULL,

    CONSTRAINT FK_USER_ID FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings
(
    id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id    bigint                      NOT NULL,
    booker_id  bigint,
    status     text                        NOT NULL DEFAULT 'WAITING',

    CONSTRAINT FK_BOOKING_ITEM_ID FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT FK_BOOKING_BOOKER_ID FOREIGN KEY (booker_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT CHK_BOOKING_STATUS CHECK (status IN ('APPROVED', 'REJECTED', 'WAITING')),
    CONSTRAINT CHK_BOOKING_DATES CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description  text   NOT NULL,
    requestor_id bigint NOT NULL,

    CONSTRAINT FK_REQUESTOR_ID FOREIGN KEY (requestor_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments
(
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text      text                        NOT NULL,
    item_id   bigint                      NOT NULL,
    author_id bigint                      NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT FK_COMMENT_ITEM_ID FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT FK_COMMENT_AUTHOR_ID FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);