#!/bin/sh

docker cp user.ldif ldap:/tmp/user.ldif

docker exec -it ldap ldapadd \
  -x \
  -H ldap://localhost:389 \
  -D "cn=admin,dc=astrodesk" \
  -w adminpassword \
  -f /tmp/user.ldif
