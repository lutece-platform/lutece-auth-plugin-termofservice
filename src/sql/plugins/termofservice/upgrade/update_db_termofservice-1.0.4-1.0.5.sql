-- liquibase formatted sql
-- changeset termofservice:update_db_termofservice-1.0.4-1.0.5.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Add column to termofservice_entry
--
ALTER TABLE termofservice_entry
ADD COLUMN title varchar(255) default '' NOT NULL;

--
-- Add column to termofservice_entry
--
ALTER TABLE termofservice_entry
ADD COLUMN published int(1) default '0';