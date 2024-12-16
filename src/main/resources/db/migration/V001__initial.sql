create sequence some_entry_seq start with 1000;

create table some_entry
(
	id          bigint  not null default nextval('some_entry_seq') primary key,
	date        date    not null,
	description varchar not null,
	duration    bigint  not null
);
