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
            "rule-action": "convert-lowercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "2",
            "rule-name": "2",
            "rule-target": "table",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "%"
            },
            "rule-action": "convert-lowercase",
            "value": null,
            "old-value": null
        },
        {
            "rule-type": "transformation",
            "rule-id": "3",
            "rule-name": "3",
            "rule-target": "schema",
            "object-locator": {
                "schema-name": "PSPADM"
            },
            "rule-action": "convert-lowercase",
            "value": null,
            "old-value": null
        },
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
            "rule-id": "22",
            "rule-name": "22",
            "object-locator": {
                "schema-name": "PSPADM",
                "table-name": "PSP_PAYCHECK_USAGE"
            },
            "parallel-load": {
                "type": "partitions-auto"
            }
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
                        "0ffec4a7-0a53-42ce-ad47-1ca311cdcc25"
                    ],
                    [
                        "2000ba95-4930-4c17-b1d8-bf9b660f70c8"
                    ],
                    [
                        "2ffcec4e-018c-4456-b17b-5398fde653bd"
                    ],
                    [
                        "3ff579c9-8b7f-4cb9-b9d9-f452910aaec8"
                    ],
                    [
                        "4ff977aa-96a0-46a9-8ae3-6b073a1fbf82"
                    ],
                    [
                        "5ffc9d54-4a0b-4270-8794-f124819cb688"
                    ],
                    [
                        "6ffd15fe-b1a5-4d84-b622-893b9b36d98e"
                    ],
                    [
                        "7ff2e5cc-7ecf-4456-b3d2-98a5f719e32e"
                    ],
                    [
                        "8ff85295-22d1-4f15-8e4a-7bd3b19b8fb7"
                    ],
                    [
                        "9ff911cf-7fa1-4a35-855e-740461e66425"
                    ],
                    [
                        "aff5be8b-b574-4281-a18e-bec0ba759e1d"
                    ],
                    [
                        "bffd2e34-2a5f-41a8-92d2-817b7292669a"
                    ],
                    [
                        "d00152d2-85eb-4112-9e9d-fec013a0eae0"
                    ],
                    [
                        "dffb82fa-edca-4754-b87b-4e1cde97ba81"
                    ],
                    [
                        "f0025c23-dfc6-467a-b877-e967a063643c"
                    ],
                    [
                        "ffffffee-0a0f-4cb0-9714-bc85d1ea6227"
                    ]
                ]
            }
        }
    ]
}
