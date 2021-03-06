--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: blog_categories; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE blog_categories (
    id integer NOT NULL,
    name character varying(20) NOT NULL
);


--
-- Name: blogserial; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE blogserial
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: blog_item; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE blog_item (
    inumber integer DEFAULT nextval('blogserial'::regclass) NOT NULL,
    ititle character varying(160) DEFAULT NULL::character varying,
    ibody text NOT NULL,
    imore text,
    itime timestamp without time zone NOT NULL,
    mtime timestamp without time zone,
    blog_section_name character varying(30) DEFAULT 'Main'::character varying NOT NULL
);


--
-- Name: blog_item_categories; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE blog_item_categories (
    item_id integer NOT NULL,
    category_id integer NOT NULL
);


--
-- Name: comments_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE comments_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: blog_item_comments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE blog_item_comments (
    comment_id bigint DEFAULT nextval('comments_seq'::regclass) NOT NULL,
    blog_entry_id integer NOT NULL,
    comments text NOT NULL,
    visible boolean DEFAULT true,
    created timestamp without time zone DEFAULT now() NOT NULL,
    updated timestamp without time zone
);


--
-- Name: blog_section; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE blog_section (
    name character varying(30) NOT NULL
);


--
-- Name: blog_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY blog_categories
    ADD CONSTRAINT blog_categories_pkey PRIMARY KEY (id);


--
-- Name: blog_item_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY blog_item_comments
    ADD CONSTRAINT blog_item_comments_pkey PRIMARY KEY (comment_id);


--
-- Name: blog_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY blog_item
    ADD CONSTRAINT blog_item_pkey PRIMARY KEY (inumber);


--
-- Name: blog_section_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY blog_section
    ADD CONSTRAINT blog_section_pkey PRIMARY KEY (name);


--
-- Name: item_category; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY blog_item_categories
    ADD CONSTRAINT item_category PRIMARY KEY (item_id, category_id);


--
-- Name: blog_section_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX blog_section_name_idx ON blog_item USING btree (blog_section_name);


--
-- Name: blog_item_categories_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY blog_item_categories
    ADD CONSTRAINT blog_item_categories_category_id_fkey FOREIGN KEY (category_id) REFERENCES blog_categories(id);


--
-- Name: blog_item_categories_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY blog_item_categories
    ADD CONSTRAINT blog_item_categories_item_id_fkey FOREIGN KEY (item_id) REFERENCES blog_item(inumber);


--
-- Name: blog_item_comments_blog_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY blog_item_comments
    ADD CONSTRAINT blog_item_comments_blog_entry_id_fkey FOREIGN KEY (blog_entry_id) REFERENCES blog_item(inumber);


--
-- PostgreSQL database dump complete
--

