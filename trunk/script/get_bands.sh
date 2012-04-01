#!/bin/sh
#
# Extract band information from AID.
#
# username is $1

mysql -h db.aavso.org -u $1 -P 3306 -D aid -p -e "select Code, Description, shortName, R_color, G_color, B_color from bands order by Code";
