#!/bin/sh
#
# Extract band information from AID.
#
# username is $1

mysql -h 65.118.148.197 -u $1 -P 3307 -D aid -p -e "select Code, Description, shortName, R_color, G_color, B_color from bands order by Code";
