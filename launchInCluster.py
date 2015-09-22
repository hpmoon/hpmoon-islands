from __future__ import division
import os
import sys
import paramiko
import time

populationSize = 1024
numberOfIslands = [4]
disjoint = ["true"]
dimension = [512]
problems = ["zdt1"]
numJobs = 5

#numberOfIslands = [8,32,128]
#disjoint = ["true","false","none"]
#dimension = [512,2048]
#problems = ["zdt1","zdt2","zdt3","zdt6"]
#DONT FORGET TIME!!!!! AND 
runtime = "10000"
baseFile = "baseFileIsland.params"
baseServerFileName = "baseServer.params"


serverIp = "localhost"
serverPort = "8999"
islandIdHeader = "isla"
jdk = "/home/pgarcia/jdk1.8.0_45/bin/java"
launchDir = "/home/pgarcia/NetBeansProjects/ECJ/build/classes"


def generateServerFile(serverfilename, numIsls):
    with open(baseServerFileName) as f:
        lines = f.readlines()
        lines = [l for l in lines]
        with open(serverfilename, "w") as f1:
            f1.writelines(lines)
            f1.write("exch.server-addr = "+serverIp+"\n")
            f1.write("exch.server-port = "+serverPort+"\n")
            f1.write("exch.num-islands = "+`numIsls`+"\n");
            f1.write("\n") 
            for i in range(0,numIsls):
                f1.write("exch.island."+`i`+".id = "+islandIdHeader+`i`+"\n");
                f1.write("exch.island."+`i`+".num-mig = "+`numIsls-1`+"\n");
                otherIsland = 0
                for o in range(0,numIsls-1):
                    if(o == i):
                        otherIsland = otherIsland+1
                    f1.write("exch.island."+`i`+".mig."+`o`+" = "+islandIdHeader+`otherIsland`+"\n");
                    otherIsland = otherIsland+1
                f1.write("\n");
                

#def keepLastLines(numLines):

#iId starts in 0!
def runIslandFile(runfile, iId):
    hostname = "compute-0-"+`(iId+1)%16`
    return
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname)
    print("CONNECTED TO "+hostname)
    command = "cd "+launchDir+";"+jdk+" ec.Evolve -file "+runfile;
    print("Executing command: "+command)
    
    ssh.exec_command(command)
    

for ni in numberOfIslands:
    serverFileName = "server"+`ni`+"_.params"
    generateServerFile(serverFileName,ni)
    for d in disjoint:
        for dim in dimension:
            for p in problems:
                for j in range(0,numJobs):
                    for islId in range(0,ni):
                        fileheader = "job."+`j`+"."+`ni`+"_"+d+"_"+`dim`+"_"+p+"_id_"+`islId`
                        runfile = fileheader+"_.params"
                        subpopsize = populationSize//ni
                        mutationProb = 1/dim
                        chunkSize = dim//ni
                        if d == "true":
                            mutationProb = 1/chunkSize
                        if d == "false":
                            mutationProb = 1/(chunkSize*3)
                        with open(baseFile) as f:
                            lines = f.readlines()
                            lines = [l for l in lines]
                            with open(runfile, "w") as f1:
                                f1.writelines(lines)
                                f1.write("#AUTOGENERATED STUFF \n")
                                f1.write("eval.problem.type = "+p+"\n");
                                f1.write("pop.subpop.0.size ="+`subpopsize`+"\n")
                                f1.write("pop.subpop.0.species.genome-size = "+`dim`+"\n");
                                f1.write("pop.subpop.0.species.mutation-prob = "+`mutationProb`+"\n");
                                f1.write("pop.subpop.0.species.pipe.disjoint = "+d+"\n");
                                f1.write("pop.subpop.0.species.pipe.source.0.disjoint = "+d+"\n");
                                f1.write("pop.subpop.0.species.min-gene = 0\n");	
                                f1.write("pop.subpop.0.species.max-gene = 1\n"); 
                                f1.write("eval.runtime = "+runtime+"\n");
                                f1.write("hpmoon.num-islands = "+`ni`+"\n")
                                f1.write("hpmoon.island-id = "+`islId`+"\n")
                                f1.write("stat.front = "+fileheader+".front\n")
                                f1.write("stat.file  = "+fileheader+".stats\n")
                                f1.write("seed.0 = 123"+`islId`+"\n")
                                f1.write("exch.id = isla"+`islId`+"\n")
                                port = 9000+islId
                                f1.write("exch.client-port = "+`port`+"\n")
                                f1.write("exch.server-addr = "+serverIp+"\n")
                                f1.write("exch.server-port = "+serverPort+"\n")
                        print("RUNNING ISLAND FILE "+runfile)
                        runIslandFile(runfile,islId)

                    #os.system(jdk+" ec.Evolve -file "+runfile)
                    print("RUNNING SERVER: "+serverFileName)
                    os.system(jdk+" es.ugr.hpmoon.IslandRandomExchange -file "+serverFileName)
                    time.sleep(5)
                    
