CREATE TABLE IF NOT EXISTS guilds (
    guild_id BIGINT PRIMARY KEY NOT NULL DEFAULT -1,
    log_channel BIGINT NOT NULL DEFAULT -1,
    prefix VARCHAR(5) NOT NULL DEFAULT 'mk!',
    language VARCHAR(5) NOT NULL DEFAULT 'en_US'
);