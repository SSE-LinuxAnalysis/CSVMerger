# CSVMerger

### [csvmerger.jar](http://jenkins.sse.uni-hildesheim.de/job/CSVMerger/lastSuccessfulBuild/artifact/build/jars/csvmerger.jar)
`java -jar csvmerger.jar <inputA> <colsA> <inputB> <colsB> <output>`

- `inputA`: small csv file
- `colsA`: comma separated column indices for inputA (zero based)
- `inputB`: big csv file
- `colsB`: comma separated column indices for inputB (zero based)
- `output`: output csv file

Copy all rows of `inputB` to `output` that have at least one matching row in `inputA`. With `colsA` and `colsB` you can specify which columns have to be compared.


### [excelmerger.jar](http://jenkins.sse.uni-hildesheim.de/job/CSVMerger/lastSuccessfulBuild/artifact/build/jars/excelmerger.jar)
`java -jar excelmerger.jar <csv input file> <folder with xlsx files> <output csv file>`
Search for .xlsx files in the given folder and copy all 'ERROR' rows that have a matching row in the csv input file to the output file.
