<html>
    <head>
        <title>Model Report ${time}</title>
    </head>
    <body>
        <table border="1">
            <tr>
                <th>Seriel No.</th>
                <th>Rule Name</th>
                <th>Testcase</th>
            <#list models as value>
                <th width="200">${value}</th>
            </#list>
            </tr>
        <#list rows as row>
            <tr>
                <td>${row.seriaNo}</td>
                <td>${row.ruleName}</td>
                <td>${row.testcaseName}</td>
            <#list row.workflowTime as value>
                <td>${value}</td>
            </#list>
            </tr>
        </#list>
        </table>
    </body>
</html>