# Initialize Grid'5000

- Log-in to [Grid'5000](https://www.grid5000.fr/w/Grid5000:Home) with your credentials and add your SSH key
- From a terminal, connect to Grid'5000 with `ssh <user>@access.grid5000.fr` and to a site with `ssh site` (for example `ssh nancy`)
- execute the command `git clone https://gitlab.ensimag.fr/syst-me-distribu-s/systeme-distribues.git`
- execute `cd systeme-distribues/`
- execute `git checkout grid`
- execute `cd grid/`
- to reserve nodes, execute `./oarsub.sh <nodes> <time>`. For example, to reserve 5 nodes for one hour and half `./oarsub.sh 5 01:30`
- to install debian on all nodes and run the taktuk installer, `./deploy.sh <makefile>`. taktuk runs as root and executes the instructions in `taktuk.sh`. ( Exec the system)
** REMARK: `<makefile>` must be in ./systeme-distribues/akka-sample-cluster-scala

## To-do 
Take inspiration from the project (Distribuited make) in dist_make and the example in akka-cluster of this folder to write "The distributed make" with akka-cluster.
 
