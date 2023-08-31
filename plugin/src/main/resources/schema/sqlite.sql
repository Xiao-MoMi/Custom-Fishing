CREATE TABLE IF NOT EXISTS `{prefix}_data`
(
    `uuid`       char(36)    NOT NULL UNIQUE,
    `lock`       INT         NOT NULL,
    `data`       longblob    NOT NULL,
    PRIMARY KEY (`uuid`)
);