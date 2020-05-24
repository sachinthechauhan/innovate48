
create database quickdonate;
CREATE USER 'quickdonate'@'localhost' IDENTIFIED BY 'quickdonate';	 
	 	 
GRANT ALL PRIVILEGES ON quickdonate.* TO 'quickdonate'@'localhost';	 
FLUSH PRIVILEGES