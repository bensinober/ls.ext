CREATE DATABASE IF NOT EXISTS sibyl;
GRANT ALL ON sibyl.* TO '$SIBYL_USER'@'%' IDENTIFIED BY '$SIBYL_PASSWORD';
