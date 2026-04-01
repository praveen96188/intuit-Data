. /l/$1
  
cd $GG_HOME
char7_8=${RDS_SID:6:2}
if [ `echo $char7_8 |grep "01" |wc -l` -gt 0 ]; then
  dr_rds_sid=${RDS_SID:0:3}os${RDS_SID:5}
  tmp_rds_sid=${RDS_SID:0:3}ot${RDS_SID:5}
  new_rds_sid=${RDS_SID:0:3}on${RDS_SID:5}
elif [ `echo $char7_8 |grep "ib" |wc -l` -gt 0 ]; then
  dr_rds_sid=${RDS_SID:0:3}sp${RDS_SID:5}
  tmp_rds_sid=${RDS_SID:0:3}tp${RDS_SID:5}
  new_rds_sid=${RDS_SID:0:3}np${RDS_SID:5}
fi
echo "dr_rds_sid=$dr_rds_sid"
echo "tmp_rds_sid=$tmp_rds_sid"
echo "new_rds_sid=$new_rds_sid"

echo ""
echo "Create files for temp replicat"
old_replicat=`ls $GG_HOME/dirprm/r*s*.prm |cut -d'/' -f7 |cut -d'.' -f1`
echo "old_replicat=$old_replicat"
tmp_replicat=${old_replicat/s/t}
echo "tmp_replicat=$tmp_replicat"

echo "Create $GG_HOME/diroby/create_${tmp_replicat}.oby"
cp $GG_HOME/diroby/create_${old_replicat}.oby $GG_HOME/diroby/create_${tmp_replicat}.oby
sed -i -e "s/${old_replicat}/${tmp_replicat}/g" $GG_HOME/diroby/create_${tmp_replicat}.oby
sed -i -e "s/${dr_rds_sid}/${tmp_rds_sid}/g" $GG_HOME/diroby/create_${tmp_replicat}.oby

echo "diff $GG_HOME/diroby/create_${old_replicat}.oby $GG_HOME/diroby/create_${tmp_replicat}.oby"
diff $GG_HOME/diroby/create_${old_replicat}.oby $GG_HOME/diroby/create_${tmp_replicat}.oby

echo "Create $GG_HOME/dirprm/${tmp_replicat}.prm"
cp $GG_HOME/dirprm/${old_replicat}.prm $GG_HOME/dirprm/${tmp_replicat}.prm
sed -i -e "s/${old_replicat}/${tmp_replicat}/g" $GG_HOME/dirprm/${tmp_replicat}.prm
sed -i -e "s/${dr_rds_sid}/${RDS_SID}/g" $GG_HOME/dirprm/${tmp_replicat}.prm

echo "diff $GG_HOME/dirprm/${old_replicat}.prm $GG_HOME/dirprm/${tmp_replicat}.prm"
diff $GG_HOME/dirprm/${old_replicat}.prm $GG_HOME/dirprm/${tmp_replicat}.prm

echo ""
echo "Create files for temp pump"
old_pump=`ls $GG_HOME/dirprm/p*qbs*.prm |cut -d'/' -f7 |cut -d'.' -f1`
echo "old_pump=$old_pump"

pchar7_8=${old_pump:3:2}
if [ `echo $pchar7_8 |grep "bs" |wc -l` -gt 0 ]; then
  tmp_pump=${old_pump:0:3}bt${old_pump:5}
elif [ `echo $pchar7_8 |grep "sp" |wc -l` -gt 0 ]; then
  tmp_pump=${old_pump:0:3}tp${old_pump:5}
fi
echo "tmp_pump=$tmp_pump"

echo "Create $GG_HOME/diroby/create_${tmp_pump}.oby"
cp $GG_HOME/diroby/create_${old_pump}.oby $GG_HOME/diroby/create_${tmp_pump}.oby
sed -i -e "s/${old_pump}/${tmp_pump}/g" $GG_HOME/diroby/create_${tmp_pump}.oby
sed -i -e "s/add rmttrail .\/dirdat\/${RDS_SID}/add rmttrail .\/dirdat\/${tmp_rds_sid}/g" $GG_HOME/diroby/create_${tmp_pump}.oby

echo "diff $GG_HOME/diroby/create_${old_pump}.oby $GG_HOME/diroby/create_${tmp_pump}.oby"
diff $GG_HOME/diroby/create_${old_pump}.oby $GG_HOME/diroby/create_${tmp_pump}.oby

echo "Create $GG_HOME/dirprm/${tmp_pump}.prm"
cp $GG_HOME/dirprm/${old_pump}.prm $GG_HOME/dirprm/${tmp_pump}.prm
sed -i -e "s/${old_pump}/${tmp_pump}/g" $GG_HOME/dirprm/${tmp_pump}.prm
sed -i -e "s/${RDS_SID}/${tmp_rds_sid}/g" $GG_HOME/dirprm/${tmp_pump}.prm
sed -i -e "s/gg${dr_rds_sid}/gg${RDS_SID}/g" $GG_HOME/dirprm/${tmp_pump}.prm

echo "diff $GG_HOME/dirprm/${old_pump}.prm $GG_HOME/dirprm/${tmp_pump}.prm"
diff $GG_HOME/dirprm/${old_pump}.prm $GG_HOME/dirprm/${tmp_pump}.prm

echo ""
echo "Modify $GG_HOME/dirprm/mgr.prm"
if [ ! -f $GG_HOME/dirprm/mgr.prm.ORG ]; then
  cp $GG_HOME/dirprm/mgr.prm $GG_HOME/dirprm/mgr.prm.ORG
fi
cp $GG_HOME/dirprm/mgr.prm.ORG $GG_HOME/dirprm/mgr.prm
echo "PURGEOLDEXTRACTS ./dirdat/${tmp_rds_sid}/tr*, USECHECKPOINTS, MINKEEPDAYS 10" >> $GG_HOME/dirprm/mgr.prm

echo "diff $GG_HOME/dirprm/mgr.prm $GG_HOME/dirprm/mgr.prm.ORG"
diff $GG_HOME/dirprm/mgr.prm $GG_HOME/dirprm/mgr.prm.ORG

echo "Create directory $GG_HOME/dirdat/${tmp_rds_sid}"
mkdir -p $GG_HOME/dirdat/${tmp_rds_sid}

