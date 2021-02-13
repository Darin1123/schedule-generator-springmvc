create table user
(
    id       bigint auto_increment
        primary key,
    name     varchar(50) null,
    email    varchar(60) null,
    password varchar(64) null,
    constraint email
        unique (email)
);

create table course
(
    id        bigint auto_increment
        primary key,
    uid       bigint      null,
    name      varchar(10) null,
    author_id bigint      null,
    term      varchar(20) null,
    star      int         null,
    constraint uid
        unique (uid, name, term),
    constraint course_ibfk_1
        foreign key (uid) references user (id)
            on delete cascade
);

create table course_section
(
    cid      bigint                     not null,
    category enum ('lec', 'tut', 'lab') not null,
    sec_num  int                        not null,
    sec_time varchar(50)                null,
    primary key (cid, category, sec_num),
    constraint course_section_ibfk_1
        foreign key (cid) references course (id)
            on delete cascade
);

