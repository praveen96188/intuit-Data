1.Stop B-C3 DMS replication Tasks for 2 range tables (Task-pem, Task-6)
2.Stop C3-D3 DMS replication Tasks for 2 range tables (Task-6)
3.C3, rename 2 tables to old
4.create new partitioning tables
5.copy data from old to new table (using insert select)
6.create indexes on new tables
7.B-C3 (Replication wise no change Task-pem , Task-6)
8.C3-D3(remove this 2 tables from Task-6   )
9.Create new task for this 2 tables (task-6a)
10.Resume C3-D3(Task-6, Task-6a)
11.Resume C3-D3 first(Task-pem, Task-6)
--existing table mapping C3-D3
--Task-6

{
    "rules": [
        {
            "rule-type": "transformation",
            "rule-id": "1",
            "rule-name": "1",
            "rule-target": "column",
            "object-locator": {
                "schema-name": "%",
                "table-name": "%",
                "column-name": "%"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "2",
            "rule-name": "2",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "%"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "3",
            "rule-name": "3",
            "rule-target": "schema",
            "object-locator": {
                "schema-name": "pspadm"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "selection",
            "rule-id": "1312",
            "rule-name": "123",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_pstub_employee_info_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "151",
            "rule-name": "151",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_deduction_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "161",
            "rule-name": "161",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_paystub_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "171",
            "rule-name": "171",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_qbdt_transaction_info_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "181",
            "rule-name": "181",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_disburse_advice_tax_liab_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "202",
            "rule-name": "202",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_pstub_paid_timeoff_item_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        
        {
            "rule-type": "transformation",
            "rule-id": "33",
            "rule-name": "33",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_disburse_advice_tax_liab_p%"
            },
            "rule-action": "rename",
            "value": "PSP_DISBURSE_ADVICE_TAX_LIAB",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "4",
            "rule-name": "4",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_qbdt_transaction_info_p%"
            },
            "rule-action": "rename",
            "value": "PSP_QBDT_TRANSACTION_INFO",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "5",
            "rule-name": "5",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_paystub_p%"
            },
            "rule-action": "rename",
            "value": "PSP_PAYSTUB",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "6",
            "rule-name": "6",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_deduction_p%"
            },
            "rule-action": "rename",
            "value": "PSP_DEDUCTION",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "7",
            "rule-name": "7",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_pstub_paid_timeoff_item_p%"
            },
            "rule-action": "rename",
            "value": "PSP_PSTUB_PAID_TIMEOFF_ITEM",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "8",
            "rule-name": "8",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_pstub_employee_info_p%"
            },
            "rule-action": "rename",
            "value": "PSP_PSTUB_EMPLOYEE_INFO",
            "old-value": null
        }
    ]
}





---Task-6a
{
    "rules": [
        {
            "rule-type": "transformation",
            "rule-id": "1",
            "rule-name": "1",
            "rule-target": "column",
            "object-locator": {
                "schema-name": "%",
                "table-name": "%",
                "column-name": "%"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "2",
            "rule-name": "2",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "%"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "3",
            "rule-name": "3",
            "rule-target": "schema",
            "object-locator": {
                "schema-name": "pspadm"
            },
            "rule-action": "convert-uppercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "selection",
            "rule-id": "191",
            "rule-name": "191",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_paycheck_usage_20%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "selection",
            "rule-id": "201",
            "rule-name": "201",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_entitlement_message_p%"
            },
            "rule-action": "include",
            "filters": []
        },
        {
            "rule-type": "transformation",
            "rule-id": "11",
            "rule-name": "11",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_entitlement_message_p%"
            },
            "rule-action": "rename",
            "value": "PSP_ENTITLEMENT_MESSAGE",
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "22",
            "rule-name": "22",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "pspadm",
                "table-name": "psp_paycheck_usage_20%"
            },
            "rule-action": "rename",
            "value": "PSP_PAYCHECK_USAGE",
            "old-value": null
        },



