KNIME Lib Dependency Problem
=======

## Exception: Execute Failed ***.TransitionSystem
This is due to the class loading in runtime. TransitionSystem is from another jar from ProM. When it works well in ProM 
but the jar can not be found in KNIME, then the class can not be entered,and execution fails.

**Solution**:   add TransitionSystem.jar directly into KNIME lib, and then rebuild, it works. 

## Exception: Node can not be created
Still due to the link problem, simplify the structure of added libraries of jars. 

**Solution**:   
* Add *.jar into lib of source
* Configure it through MANIFEST.MF--> Runtime
