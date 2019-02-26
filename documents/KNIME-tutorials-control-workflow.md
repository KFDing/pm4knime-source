Control Workflow
=========
## Goal

Parameterize the workflow to benefit the automation of multiple input factors during the execution. 

## Loop Workflow

In [https://www.knime.com/nodeguide/control-structures/loops/example-for-recursive-replacement-of-strings] 
describes the use of loop to recursively execute the workflow. _**Recursive Loop Start/End**_ is used to build
the structure. For Recursive Loop End, there are at least 2 out ports, port 0 to output the data, other output ports data
will be passed again into the loop.

**Attention!!!** 

There are hidden flow variables in Node _**XX_Loop_Start/End**_. They are local flow variables and defined by the node. 
We can connect the other nodes to use the flow variables. Even output them into the data table. 



## Link
https://www.knime.com/nodeguide/control-structures

