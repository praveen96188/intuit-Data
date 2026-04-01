python3 Non_Prod_HC_script.py --host $1 --dbname $2 --user $3 --password-file .pp

if [ "$2" == "sysapgib" ]; then
    mv *sysapgib*.txt *sysapgib*.html "sysapgib/" 2>/dev/null
     echo "Log files have been moved to sysapgib/"
fi
if [ "$2" == "psyspg01" ]; then
    mv *psyspg01*.txt *psyspg01*.html "psyspg01/" 2>/dev/null
     echo "Log files have been moved to psyspg01/"
fi
if [ "$2" == "ppspsodb" ]; then
    mv *ppspsodb*.txt *ppspsodb*.html "ppspsodb/" 2>/dev/null
     echo "Log files have been moved to ppspsodb/"
fi
if [ "$2" == "psppdarc" ]; then
    mv *psppdarc*.txt *psppdarc*.html "psppdarc/" 2>/dev/null
     echo "Log files have been moved to psppdarc/"
fi
if [ "$2" == "pdsibobdb" ]; then
    mv *pdsibobdb*.txt *pdsibobdb*.html "pdsibobdb/" 2>/dev/null
     echo "Log files have been moved to pdsibobdb/"
fi
if [ "$2" == "ppdspg02" ]; then
    mv *ppdspg02*.txt *ppdspg02*.html "ppdspg02/" 2>/dev/null
     echo "Log files have been moved to ppdspg02/"
fi
