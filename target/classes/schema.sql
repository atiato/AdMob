/*drop table if exists bank_details;

CREATE TABLE bank_details (
  id int(4) NOT NULL,
  bank_code varchar(20) NOT NULL,
  bank_name varchar(50) NOT NULL,
  branch varchar(50) NOT NULL,
  phone varchar(30) NOT NULL,
  email varchar(60) NOT NULL,
  log_date datetime DEFAULT CURRENT_TIMESTAMP,
  username varchar(40) NOT NULL
);

ALTER TABLE bank_details
ADD PRIMARY KEY (bank_name,branch),
ADD UNIQUE KEY id (id);

ALTER TABLE bank_details
MODIFY id int(4) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;


drop table if exists op_config;

CREATE TABLE op_config (
  id int(4) NOT NULL,
  operator_code varchar(20) NOT NULL,
  operator_name varchar(60) NOT NULL,
  cmc_coe_ratio_local double NOT NULL,
  cmc_coe_ratio_roaming double NOT NULL,
  operator_tax double NOT NULL,
  payment_type_id int(4) NOT NULL,
  log_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  username varchar(40) NOT NULL,
   phone varchar(30) NOT NULL,
  email varchar(60) NOT NULL,
  note  varchar(60) NOT NULL
);


ALTER TABLE op_config
  ADD PRIMARY KEY (operator_name),
  ADD UNIQUE KEY id (id);


ALTER TABLE op_config
  MODIFY id int(4) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;


drop table if exists operator_banking_accounts;

CREATE TABLE operator_banking_accounts (
  id int(4) NOT NULL,
  operator_id int(4) NOT NULL,
  bank_id int(4) NOT NULL,
  account_number varchar(100) NOT NULL,
  log_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  username varchar(50) NOT NULL,
  stopping_date datetime DEFAULT '3000-01-01 00:00:00',
  currency varchar(10)
);


ALTER TABLE operator_banking_accounts
  ADD PRIMARY KEY (account_number),
  ADD UNIQUE KEY id (id);


ALTER TABLE operator_banking_accounts
  MODIFY id int(4) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;*/


