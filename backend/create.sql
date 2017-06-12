create table authority (id varchar(255) not null, name varchar(255) not null, primary key (id))
create table site_user (id varchar(255) not null, enabled boolean not null, last_password_reset_date timestamp not null, password varchar(255) not null, username varchar(255) not null, primary key (id))
create table site_user_authorities (site_user_id varchar(255) not null, authorities_id varchar(255) not null)
alter table authority add constraint UK_jdeu5vgpb8k5ptsqhrvamuad2 unique (name)
alter table site_user add constraint UK_jerlw3g2urnh55wcrm2b5kqnj unique (username)
alter table site_user_authorities add constraint FKaxnrtb50r89252bjrrsgj0n39 foreign key (authorities_id) references authority
alter table site_user_authorities add constraint FKp23eryn02dex9bperx4pkd7bm foreign key (site_user_id) references site_user
