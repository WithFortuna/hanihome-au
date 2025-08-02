-- PostgreSQL 기준
ALTER TABLE user_preferred_regions
ALTER COLUMN latitude TYPE double precision;

ALTER TABLE user_preferred_regions
ALTER COLUMN longitude TYPE double precision;
