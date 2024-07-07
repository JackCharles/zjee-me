CREATE TABLE IF NOT EXISTS cmd_task(
    id          int primary key auto_increment comment 'id',
    task_id     varchar(64)  comment 'task id',
    cmd         varchar(255) comment 'execute cmd',
    time_out    int comment 'cmd time out',
    start_time  varchar(32)  comment 'process start time',
    end_time    varchar(32)  comment 'process end time',
    exit_status int comment 'exit value',
    desc        varchar(255)  comment 'cmd description'
) comment 'cmd task info';

create table if not exists users(
    username varchar(50) not null primary key,
    password varchar(500) not null,
    enabled boolean not null
) comment 'users';

create table if not exists authorities (
    username varchar(50) not null,
    authority varchar(50) not null
)comment 'authorities';
