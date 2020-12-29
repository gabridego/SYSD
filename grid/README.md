# Initialize Grid'5000

- Log-in to [Grid'5000](https://www.grid5000.fr/w/Grid5000:Home) with your credentials and add your SSH key
- From a terminal, connect to Grid'5000 with `ssh <user>@access.grid5000.fr` and to a site with `ssh site` (for example `ssh nancy`)
- git clone the repo
- to reserve nodes, execute `./oarsub.sh <nodes> <time>`. For example, to reserve 5 nodes for one hour and half `./oarsub.sh 5 01:30`
- to install debian on all nodes and run the taktuk installer, `./deploy.sh`. taktuk runs as root and executes the instructions in `taktuk.sh`.

## To-do

Create an ansible or puppet file to install scala and akka dependencies on all nodes