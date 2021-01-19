TAKTUK_EXEC="${PWD}/taktuk_exec.sh"
TAKTUK_EXEC_MASTER="${PWD}/taktuk_exec_master.sh"

if [[ $# -ne 2 ]]
then
  echo "Argument not correct, put number of workers and port"
  exit
fi

taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | tail -n +2 | head -$1 ) broadcast exec [ $TAKTUK_EXEC $2 ] &
taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | head -1 ) broadcast exec [ $TAKTUK_EXEC_MASTER $2 ]
