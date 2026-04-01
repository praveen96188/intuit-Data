# How do I run this script?

Example configuration:

* **deploy.json** must contain a `targets` section, spelling out the `account_id` and `region` for your source clusters, as well as all snapshot copy destinations.

```json
{
  "targets": [
    {
      "name": "source-target",
      "account_id": "1111-2222-3333",
      "region": "us-west-2"
    },
    {
      "name": "cross-region-target",
      "account_id": "1111-2222-3333",
      "region": "us-east-2"
    },
    {
      "name": "cross-account-target",
      "account_id": "4444-5555-6666",
      "region": "us-east-2"
    }
  ]
}
```

* **config.json** must specify a `snapshots` section, nested inside either a `cluster` or a `group`.
  * If a whole `group` has a snapshot configuration, then every cluster in that group will be snapshotted on the same schedule, and copied to the same destination(s).
  * The `source_target` and `dest_target` must refer to targets defined in **deploy.json** (see above).
  * The `backup_schedule` (a cron expression) and `retention_days` parameters are optional.

```json
"snapshots": {
  "name": "snap1",
  "backup_schedule": "0 1 * * ? *",
  "source_target": "source-target",
  "copy_targets": [
    {
      "dest_target": "cross-region-copy",
      "retention_days": 3
    },
    {
      "dest_target": "cross-account-copy",
      "retention_days": 7
    }
  ]
}
```

After filling in this config, simply run `./manage-snapshots.sh` and the script should do the rest.  Adding the `-v` option will perform a verification of existing resources (CFN stacks and KMS keys) instead.