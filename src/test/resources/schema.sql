drop table car if exists;

drop table driver if exists;


create table car
(
  id            bigint       not null,
  convertible   boolean      not null,
  date_created  timestamp    not null,
  engine_type   varchar(255),
  license_plate varchar(255) not null,
  manufacturer  varchar(255),
  rating        float,
  seat_count    smallint     not null check (seat_count >= 0),
  driver_fk     bigint,
  primary key (id)
);

create table driver
(
  id                      bigint       not null,
  coordinate              binary(255),
  date_coordinate_updated timestamp,
  date_created            timestamp    not null,
  deleted                 boolean      not null,
  online_status           varchar(255) not null,
  password                varchar(255) not null,
  username                varchar(255) not null,
  primary key (id)
);


alter table driver
  add constraint uc_username unique (username);

alter table car
  add constraint uc_licenseplate unique (license_plate);


alter table car
  add constraint FKp8l3l2l7aivvj6t8jnykmqjh4 foreign key (driver_fk) references driver;

create sequence car_seq start with 100 increment by 1;
create sequence hibernate_sequence start with 1 increment by 1;


