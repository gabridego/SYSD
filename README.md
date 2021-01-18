Sujet du Makefile parallèle et exemples de Makefile

# Run on Grid'5000

- Move inside the `grid` directory;
- To reserve nodes, execute `./oarsub.sh <nodes> <time>`. For example, to reserve 5 nodes for one hour and half `./oarsub 5 01:30`;
- To install debian on all nodes, install libraries and execute the makefile, `./deploy.sh <nb of workers> <port>`. In total, `<nb of workers>` + 1 nodes are deployed, one for the master and the rest for the workers. The Makefile must be in `dist_make`.

After installing the operating system and the dependencies on each node, the script does:
- execute a Python script to create the configuration file for akka cluster, containing the nodes' IP addresses;
- run the Scala code for the workers on `<nb of workers>` nodes. On each worker node, a number of actor specified in the `transformation.conf` file is created;
- run the Scala code for the master on a single node. Here the Makefile is read, the tree of dependencies is built and the work is split across the workers.

Information about the elapsed time are written in `dist_make/metrics.txt`.

## Run with a different number of actors per node

The performances of the program depend not only on the number of nodes, but also on the number of workers per node. By default, two actors are created for each worker node.

After deployment, modify the file `dist_make/src/main/resources/transformation.conf`.

Then, from the `grid` directory, manually run the commands:

`taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | tail -n +2 | head -<nb of workers> ) broadcast exec [ $PATH_TO_PROJECT/grid/taktuk_exec.sh <port> ] &`

`taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | head -1 ) broadcast exec [ $PATH_TO_PROJECT/grid/taktuk_exec_master.sh <port> ]`

`<nb of workers>` and `<port>` must be the same as used when executing `deploy.sh` at the previous step.
