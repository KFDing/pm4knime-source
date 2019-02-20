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
DataRow newRow = new DefaultRow(rowName, specs)
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
 DataTableSpec tableSpec = new DataTableSpec(specs);
```

Above codes explain that DataTableSpec can be created from columnSpec

5. BufferedDataConstainer: It is mutable, data can add to it.
6. ExecutionContext: 

```java
DataTableSpec spec = createOutSpec();  
BufferedDataContainer cont = exec.createDataContainer(spec);
... 
// one row is added to DataContainer
cont.addRowToTable(newRow);
// for return data
// A BufferedDataTable is created from DataContainer by getTable()
PortObject[] = new BufferedDataTable[]{cont.getTable()};
```
DataTableContainer is created from ExecutionContext according to a table spec.

### To convert DataTable to variables
[TableToVariable][https://github.com/knime/knime-base/blob/master/org.knime.base/src/org/knime/base/node/flowvariable/tablerowtovariable/TableToVariableNodeModel.java]
we need PortType
``` 
new PortType[]{BufferedDataTable.TYPE}, new PortType[]{FlowVariablePortObject.TYPE}
``` 
7. PortType 
8. FlowVariable [https://www.knime.com/wiki/flow-variables]
They are control parameters for workflow, not like normal data..

The process is usually done like this, 
* get table spec by input
* iterate on columns of this table spec to get the column spec
* create new data cell according to the type of column spec
* push data cell into FlowVariables
```java
private DataCell[] createDefaultCells(final DataTableSpec spec) {
final DataCell[] cells = new DataCell[spec.getNumColumns()];
        for (int i = cells.length; --i >= 0;) {
            final DataColumnSpec c = spec.getColumnSpec(i);
            if (c.getType().isCompatible(IntValue.class)) {
                cells[i] = new IntCell(m_int.getIntValue());
}
}

...
 protected void pushVariables(final DataTableSpec variablesSpec, final DataRow currentVariables) throws Exception {
 ..
DataColumnSpec spec = variablesSpec.getColumnSpec(i);
DataType type = spec.getType();
final DataCell cell;
cell = currentVariables.getCell(i);
if (cell != null) {
                if (type.isCompatible(IntValue.class)) {
                    pushFlowVariableInt(name, ((IntValue) cell).getIntValue());
}
...
}
```
