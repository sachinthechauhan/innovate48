CREATE TABLE ITEMCATEGORY
(	 
	 ITEMCATEGORYID               INT NOT NULL auto_increment primary key,
     ITEMCATEGORY                 VARCHAR(64) unique NOT NULL,
     ITEMCATEGORYCODE             VARCHAR(30),
     ITEMCATEGORYDESC             VARCHAR(128),
     ACTIVE                       CHAR(1) NOT NULL default 'N',
     VERSION                      INT,
     WHENMODIFIED                 DATE
);

CREATE TABLE ITEM
(	 
	 ITEMID                       INT NOT NULL auto_increment primary key,
     ITEMCATEGORYID                 VARCHAR(64) unique NOT NULL,
     ITEMCODE                     VARCHAR(30),
     ITEMDESC                     VARCHAR(128),
     ACTIVE                       CHAR(1) NOT NULL default 'N',
     VERSION                      INT,
     WHENMODIFIED                 DATE
);

CREATE TABLE USER
(	
	 USERID                   VARCHAR(12) NOT NULL primary key,
     USERNAME                  VARCHAR(128),
	 USERNICKNAME              VARCHAR(128),	
	 EMAILADDRESS              VARCHAR(128),
     MOBILENUMBER              VARCHAR(35),
	 ADDRESS                   VARCHAR(128),
	 USERTYPE		           VARCHAR(128),
	 IMAGE                     BLOB,
     PASSWORD                  VARCHAR(128),
	 VERSION                   INT,
	 WHENMODIFIED              DATE NOT NULL	 
);	 
CREATE TABLE USERTYPE
(	
     USERTYPE                  VARCHAR(128),
	 USERTYPEDESC              VARCHAR(128),	
	 WHENMODIFIED              DATE NOT NULL	 
);	 
	 	 