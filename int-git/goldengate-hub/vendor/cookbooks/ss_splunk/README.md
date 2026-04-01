ss_splunk Cookbook
===============
This cookbook configures KAOS splunkforwarder.

Requirements
------------
#### Chef version:
Make sure you run Chef >= 0.10.0.
#### Platforms:
- RHEL
- CentOS

Attributes
----------
#### Symbolic Link Attributes
These attributes are under the `node['splunk']['forwarder']['symlink']` namespace.
<table>
  <tr>
    <th>Key</th>
    <th>Type</th>
    <th>Description</th>
    <th>Default</th>
  </tr>
  <tr>
    <td><tt>enable</tt></td>
    <td>Boolean</td>
    <td>enable automatic symbolic link creation</td>
    <td><tt>true</tt></td>
  </tr>
  <tr>
    <td><tt>base</tt></td>
    <td>String</td>
    <td>base directory for symbolic link</td>
    <td><tt>/logs/appname_role_env_region</tt></td>
  </tr>
</table>

#### Monitor (inputs.conf) Attributes
These attributes are under the `node['splunk']['forwarder']['inputs']['monitor']['path']` namespace.
<table>
  <tr>
    <th>Key</th>
    <th>Type</th>
    <th>Description</th>
    <th>Default</th>
  </tr>
  <tr>
    <td><tt>host</tt></td>
    <td>String</td>
    <td>Sets the host key to a static initial value</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>idx</tt></td>
    <td>String</td>
    <td>Sets the index to store events</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>sourcetype</tt></td>
    <td>String</td>
    <td>Sets the sourcetype key/field for events</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>queue</tt></td>
    <td>String</td>
    <td>Specifies where to deposit the events</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>_tcp_routing</tt></td>
    <td>String</td>
    <td>Specifies the tcpout groups to forward data</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>host_regex</tt></td>
    <td>String</td>
    <td>regex that extracts host from the file name</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>host_segment</tt></td>
    <td>String</td>
    <td>Sets the segment of the path as the host</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>source</tt></td>
    <td>String</td>
    <td>Sets the source field for events</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>crcsalt</tt></td>
    <td>String</td>
    <td>Forces to consume files that have matching CRCs</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>ignoreolderthan</tt></td>
    <td>String</td>
    <td>Causes the input to stop checking files for updates</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>followtail</tt></td>
    <td>String</td>
    <td>monitoring begins at the end of the file</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>whitelist</tt></td>
    <td>String</td>
    <td>files from this input are monitored</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>blacklist</tt></td>
    <td>String</td>
    <td>files from this input are NOT monitored</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>alwaysopenfile</tt></td>
    <td>String</td>
    <td>force to check if it has already been indexed</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>recursive</tt></td>
    <td>String</td>
    <td>monitor subdirectories found within a monitored directory</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>time_before_close</tt></td>
    <td>String</td>
    <td>The modification time delta required before closing a file on EOF</td>
    <td><tt>nil</tt></td>
  </tr>
  <tr>
    <td><tt>followsymlink</tt></td>
    <td>String</td>
    <td>follow any symbolic links within a directory it is monitoring</td>
    <td><tt>nil</tt></td>
  </tr>
</table>

Custom Field Extractions
----------
Splunk Enterprise extracts a source field from structured event data. The field must be separated by a underscore. We create 4 sets of field extractions by default.
<table>
  <tr>
    <th>Field</th>
    <th>Description</th>
    <th>Example</th>
  </tr>
  <tr>
    <td><tt>appname</tt></td>
    <td>application name</td>
    <td><tt>tto-app1-ui</tt></td>
  </tr>
  <tr>
    <td><tt>role</tt></td>
    <td>role type or tier</td>
    <td><tt>tomcat</tt></td>
  </tr>
  <tr>
    <td><tt>env</tt></td>
    <td>environment code</td>
    <td><tt>qal</tt></td>
  </tr>
  <tr>
    <td><tt>region</tt></td>
    <td>aws region code</td>
    <td><tt>us-west-2</tt></td>
  </tr>
</table>
You can add or remove fields by updating `node['splunk']['forwarder']['symlink']['base']` attribute. Contact your Splunk administrators to manage custom fields.
* SBG: Mark Russell (Mark_Russell@intuit.com)
* CTG/CTO: Jim Merritt (Jim_Merritt@intuit.com)

Usage
-----
#### ss_splunk::default
Just include `ss_splunk` in your node's `run_list`:
```json
{
  "name":"my_node",
  "run_list": [
    "recipe[ss_splunk]"
  ]
}
```
Define your Monitor (inputs.conf) attributes in your role or environment:
```json
{
  "override_attributes": {
    "splunk": {
      "forwarder": {
        "inputs": {
          "monitor": {
            "/logs/*/var/log/messages": {
              "idx": "sbg-services",
              "sourcetype": "linux_messages_syslog",
              "crcsalt": "<SOURCE>"
            },
            "/logs/*/var/log/secure": {
              "idx": "sbg-services",
              "sourcetype": "linux_secure",
              "crcsalt": "<SOURCE>"
            },
            "/logs/*/app/logs/nginx/": {
              "idx": "sbg-services",
              "blacklist": "gz$",
              "sourcetype": "nginx"
            },
            "/logs/*/app/tomcat/*/logs/catalina.out": {
              "idx": "sbg-services",
              "sourcetype": "log4j"
            },
            "/logs/*/app/tomcat/*/logs/*": {
              "idx": "sbg-services",
              "blacklist": "catalina\\.out|gz$"
            }
          }
        }
      }
    }
  }
}
```

License and Authors
-------------------
Authors: Naohito Takeuchi (Naohito_Takeuchi@intuit.com)
