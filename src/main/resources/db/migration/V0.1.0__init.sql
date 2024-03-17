create table temperature_minute (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table temperature_hour (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,
    hour    smallint not null,

    primary key (year, month, day, hour)
);

create table temperature_day (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,

    primary key (year, month, day)
);

create table temperature_month (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,

    primary key (year, month)
);

create table pressure_minute (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table pressure_hour (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,
    hour    smallint not null,

    primary key (year, month, day, hour)
);

create table pressure_day (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,

    primary key (year, month, day)
);

create table pressure_month (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,

    primary key (year, month)
);

create table humidity_minute (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table humidity_hour (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,
    hour    smallint not null,

    primary key (year, month, day, hour)
);

create table humidity_day (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,

    primary key (year, month, day)
);

create table humidity_month (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,

    primary key (year, month)
);

create table co2_minute (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table co2_hour (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,
    hour    smallint not null,

    primary key (year, month, day, hour)
);

create table co2_day (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,

    primary key (year, month, day)
);

create table co2_month (
    average text     not null,
    median  text     not null,
    max     text     not null,
    min     text     not null,
    year    smallint not null,
    month   smallint not null,

    primary key (year, month)
);
