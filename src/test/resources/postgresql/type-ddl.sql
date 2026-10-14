---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2026 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy')
CREATE TYPE app."Mood" AS ENUM ('can''t', 'OK')
CREATE TYPE empty_enum AS ENUM ()
CREATE TYPE shell_type
CREATE TYPE inventory_item AS (name text, supplier_id integer, price numeric(10, 2))
CREATE TYPE empty_composite AS ()
CREATE TYPE localized AS (label text COLLATE pg_catalog."C", tags text[], created timestamp with time zone)
CREATE TYPE float8range AS RANGE (SUBTYPE = float8, SUBTYPE_DIFF = float8mi)
CREATE TYPE app.r AS RANGE (SUBTYPE_OPCLASS = pg_catalog.text_ops, SUBTYPE = text, COLLATION = pg_catalog."C", CANONICAL = app.canonical, MULTIRANGE_TYPE_NAME = app.mr)
ALTER TYPE mood ADD VALUE IF NOT EXISTS 'great' AFTER 'happy'
ALTER TYPE mood ADD VALUE 'bad' BEFORE 'sad'
ALTER TYPE mood ADD VALUE 'new'
ALTER TYPE mood RENAME VALUE 'ok' TO 'fine'
ALTER TYPE mood OWNER TO CURRENT_USER
ALTER TYPE mood RENAME TO feeling
ALTER TYPE mood SET SCHEMA app
ALTER TYPE inventory_item RENAME ATTRIBUTE name TO title CASCADE
ALTER TYPE inventory_item ADD ATTRIBUTE sku text COLLATE "C" RESTRICT
ALTER TYPE inventory_item DROP ATTRIBUTE IF EXISTS price CASCADE
ALTER TYPE inventory_item ALTER ATTRIBUTE supplier_id SET DATA TYPE bigint RESTRICT
ALTER TYPE inventory_item ADD ATTRIBUTE sku text, DROP ATTRIBUTE price, ALTER ATTRIBUTE name TYPE varchar(80)
CREATE DOMAIN positive_integer AS integer CHECK (VALUE > 0)
CREATE DOMAIN app.label text COLLATE "C" DEFAULT 'ok' CONSTRAINT required NOT NULL CONSTRAINT nonempty CHECK (VALUE <> '')
CREATE DOMAIN nullable_integer AS integer NULL
CREATE DOMAIN percent AS numeric(5, 2) DEFAULT 0 CHECK (VALUE >= 0) CHECK (VALUE <= 100)
ALTER DOMAIN positive_integer ADD CONSTRAINT positive CHECK (VALUE > 0) NOT VALID
ALTER DOMAIN positive_integer ADD CHECK (VALUE < 100)
ALTER DOMAIN positive_integer ADD CONSTRAINT required NOT NULL
ALTER DOMAIN positive_integer SET DEFAULT 1 + 2
ALTER DOMAIN positive_integer DROP DEFAULT
ALTER DOMAIN positive_integer SET NOT NULL
ALTER DOMAIN positive_integer DROP NOT NULL
ALTER DOMAIN positive_integer DROP CONSTRAINT IF EXISTS positive CASCADE
ALTER DOMAIN positive_integer DROP CONSTRAINT positive RESTRICT
ALTER DOMAIN positive_integer RENAME CONSTRAINT positive TO strictly_positive
ALTER DOMAIN positive_integer VALIDATE CONSTRAINT positive
ALTER DOMAIN positive_integer OWNER TO SESSION_USER
ALTER DOMAIN positive_integer RENAME TO positive_number
ALTER DOMAIN positive_integer SET SCHEMA app
CREATE EXTENSION hstore
CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public
CREATE EXTENSION hstore VERSION '1.8' SCHEMA public CASCADE
CREATE EXTENSION hstore WITH VERSION version_two
ALTER EXTENSION hstore UPDATE
ALTER EXTENSION hstore UPDATE TO '1.8'
ALTER EXTENSION hstore UPDATE TO version_two
ALTER EXTENSION hstore SET SCHEMA app
ALTER EXTENSION hstore ADD TABLE app.settings
ALTER EXTENSION hstore DROP VIEW app.settings_view
ALTER EXTENSION hstore ADD MATERIALIZED VIEW app.cached_settings
ALTER EXTENSION hstore ADD FOREIGN TABLE app.remote_settings
ALTER EXTENSION hstore ADD FUNCTION app.populate_record(anyelement, hstore)
ALTER EXTENSION hstore DROP FUNCTION app.populate_record
ALTER EXTENSION hstore ADD FUNCTION app.f(IN input integer, OUT result text, VARIADIC rest text[])
ALTER EXTENSION hstore ADD PROCEDURE app.refresh()
ALTER EXTENSION hstore ADD ROUTINE app.f(double precision, timestamp with time zone)
ALTER EXTENSION hstore ADD AGGREGATE app.total(integer)
ALTER EXTENSION hstore ADD AGGREGATE app.count_all(*)
ALTER EXTENSION hstore ADD AGGREGATE app.percentile(double precision ORDER BY double precision)
ALTER EXTENSION hstore ADD CAST (text AS hstore)
ALTER EXTENSION hstore ADD TYPE app.mood
ALTER EXTENSION hstore ADD DOMAIN app.positive
ALTER EXTENSION hstore ADD COLLATION app.localized
ALTER EXTENSION hstore ADD TEXT SEARCH CONFIGURATION app.search
ALTER EXTENSION hstore ADD TEXT SEARCH DICTIONARY app.words
ALTER EXTENSION hstore ADD OPERATOR CLASS app.ops USING btree
ALTER EXTENSION hstore ADD OPERATOR FAMILY app.ops USING gist
ALTER EXTENSION hstore ADD ACCESS METHOD my_index
ALTER EXTENSION hstore ADD FOREIGN DATA WRAPPER my_wrapper
ALTER EXTENSION hstore ADD EVENT TRIGGER install_hook
ALTER EXTENSION hstore ADD PROCEDURAL LANGUAGE plpgsql
ALTER EXTENSION hstore ADD TRANSFORM FOR hstore LANGUAGE plpython3u
ALTER EXTENSION hstore ADD OPERATOR app.&& (integer, integer)
ALTER EXTENSION hstore DROP OPERATOR = (hstore, hstore)
ALTER EXTENSION hstore ADD OPERATOR - (NONE, integer)
ALTER EXTENSION hstore ADD OPERATOR app.-> (hstore, text)
ALTER EXTENSION hstore DROP OPERATOR <<= (inet, inet)
