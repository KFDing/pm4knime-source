KNIME Concepts Documents
===
This folder is used to give tutorials on KNIME specialized on the integration of ProM. 
This file describes the concepts of KNIME in order to understand it better. 

## KNIME General Concepts
This section introduces the normal KNIME concepts, mainly by giving the link to online documents. 
But the features of KNIME concepts, which benefits the programming in KNIME will be addressed here.

1. DataCell: smallest unit to store data, like double, numeric, sting.   
It is initialized by corresponding cell, 
```
DataCell cell = DataType.getMissingCell();
double dValue = peekFlowVariableDouble(c.getFirst());
cell = new DoubleCell(dValue);

String sValue = peekFlowVariableString(c.getFirst());
sValue == null ? "" : sValue;
cell = new StringCell(sValue);
```
2. DataColumn: 
3. DataColumnSpec: 
```
DataCell[] specs = new DataCell[vars.size()];
...
specs[i] = cell;
new DefaultRow(rowName, specs)
```
An array of DataCell can be used to create dafult row, but type of each element can be different. Later, if we want to 
create a table, we need to keep the column of the same type as the first row.

4. DataTable: Immutable, which means the data kept in it are only-readable. 
5. DataTableSpec: 
Considering the following codes refered from [knime-base on github][https://github.com/knime/knime-base/blob/master/org.knime.base/src/org/knime/base/node/flowvariable/variabletotablerow2/VariableToTable2NodeModel.java]
_how to convert one variable to data table?_
```java
DataColumnSpec[] specs = new DataColumnSpec[vars.size()];  
 ...  
 specs[i] = new DataColumnSpecCreator(c.getFirst(), type).createSpec();  
 DataTableSpec tabelSpec = new DataTableSpec(specs);
```

Above codes explain that DataTableSpec can be created from columnSpec

5. BufferedDataConstainer: It is mutable, data can add to it.
6. ExecutionContext: 

```java
DataTableSpec spec = createOutSpec();  
BufferedDataContainer cont = exec.createDataContainer(spec);
```
DataTableContainer is created from ExecutionContext according to a table spec.

