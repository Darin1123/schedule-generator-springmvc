drop database if exists sgnew;

create database if not exists sgnew;

use sgnew;

create table if not exists user (
	id bigint not null auto_increment,
	name varchar(50),
	email varchar(60),
	password varchar(64),
	primary key (id),
	unique(email)
);

create table if not exists course (
	id bigint not null auto_increment,
	uid bigint,
	name varchar(10),
	author_id bigint,
	term varchar(20),
	star int,
	unique(uid, name, term),
	primary key (id),
	foreign key (uid) references user(id)
		on delete cascade
);

create table if not exists course_section (
	cid bigint,
	category enum('lec', 'tut', 'lab'),
	sec_num int,
	sec_time varchar(50),
	primary key (cid, category, sec_num),
	foreign key (cid) references course(id)
		on delete cascade
);