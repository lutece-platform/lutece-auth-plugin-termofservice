-- liquibase formatted sql
-- changeset termofservice:update_db_termofservice-2.0.0-2.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Rename column text to content in termofservice_entry
-- (text is rewritten as a type by the HSQLDB conversion of the SQL scripts)
--
ALTER TABLE termofservice_entry
RENAME COLUMN text TO content;
