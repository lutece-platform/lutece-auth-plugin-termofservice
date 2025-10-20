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