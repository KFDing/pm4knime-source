Flow Variables
=== 

## Usage 
[https://www.knime.com/knime-introductory-course/chapter7/section1/creation-and-usage-of-flow-variables] 
#### Goal: Try to parameterize the automation of workflow. 
#### Types: There are two kinds of flow variables, 
 * **global flow variable** for the whole workflow 
 * **local flow variable** with explicit port to connect to node, it has effect on the **all nodes after the port connection**. 
 We can also transfer data value into flow variable by using node _**Table Row to Variable**_ is used to define new flow variables.
 The names of flow variables are defined by the column names.
 
 But it remains to me some questions.
 1. For customized node, does the global flow variable work on it ?? 
 
  _Yes, it works. Global flow variabel is independent of nodes, it can be configured for each node._
  Hoever, when use it, make sure that it is meaningful. 
  
 2. For customized node, doese the local flow variable work on it?? 
 
 _Even the customized node has the flow port to accept the flow variable, but only one Inport and Outport_. 
 How to change it ?? Not claer.
 
 3. If yes for local flow variable, should we add extra port for them?? 
 
 4. If we need to add them, then how?? 
 
