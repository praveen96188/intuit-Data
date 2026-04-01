. /l/orcl
/u01/ogg/psppf501/12.3.0.1.4/ggsci <<EOF > /tmp/rep.txt
info all
EOF

#repl=`grep RPSPLPD1 /tmp/rep.txt`
repl=`egrep -i "^extract|^replicat" /tmp/rep.txt`
echo -e "`date` ---> $repl" >> /u01/ogg/scripts/log/gg_replicat_status.txt
