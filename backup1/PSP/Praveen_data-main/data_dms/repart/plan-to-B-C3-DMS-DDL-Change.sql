--B-C3

1.PSP_ENTITLEMENT_MESSAGE range boundaries--> task-pem

        {
            "rule-type": "selection",
            "rule-id": "20",
            "rule-name": "20",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_ENTITLEMENT_MESSAGE"
            },
            "rule-action": "include",
            "load-order": 1
        },
        {
            "rule-type": "transformation",
            "rule-id": "5",
            "rule-name": "5",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_ENTITLEMENT_MESSAGE"
            },
            "rule-action": "convert-lowercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "table-settings",
            "rule-id": "21",
            "rule-name": "21",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_ENTITLEMENT_MESSAGE"
            },
            "parallel-load": {
                "type": "ranges",
                "columns": [
                    "ENTITLEMENT_MESSAGE_SEQ"
                ],
                "boundaries": [
                    [
                        "100104c5-48be-4689-8515-cde13d365a61"
                    ],
                    [
                        "200127aa-04b6-4f44-8d0a-a1371e76d1b1"
                    ],

2.psp_paycheck_usage partitions-auto --> task-6

        {
            "rule-type": "selection",
            "rule-id": "19",
            "rule-name": "19",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_PAYCHECK_USAGE"
            },
            "rule-action": "include",
            "load-order": 2
        },
        {
            "rule-type": "transformation",
            "rule-id": "6",
            "rule-name": "6",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_PAYCHECK_USAGE"
            },
            "rule-action": "convert-lowercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "table-settings",
            "rule-id": "22",
            "rule-name": "22",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_PAYCHECK_USAGE"
            },
            "parallel-load": {
                "type": "partitions-auto"
            }
        }


3.change DDL in postgres




