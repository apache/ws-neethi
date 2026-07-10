
Apache Neethi:

This is an implementation of WS-Policy Specification (September, 2007) which 
can be located at:  https://www.w3.org/TR/2007/REC-ws-policy-20070904/

It provides a convenient model and an API to process policy information at 
runtime and an extension model for serailization and de-serialization of 
domain specific Assertions. 

Please visit : https://ws.apache.org/neethi/ for further infomation.

Security

Neethi enforces parser and normalization budgets to reduce the risk of
algorithmic-complexity and resource-exhaustion attacks when processing
untrusted policy documents.

The following system properties can be used to tune the parser limits. If a
property is unset, blank, zero, negative, or otherwise invalid, Neethi falls
back to the default shown below.

- `org.apache.neethi.parser.maxDepth` - maximum policy nesting depth.
  Default: `256`.
- `org.apache.neethi.parser.maxElements` - maximum number of parsed elements.
  Default: `100000`.
- `org.apache.neethi.parser.maxAttributes` - maximum number of parsed
  attributes.
  Default: `10000`.
- `org.apache.neethi.remote.maxPolicyBytes` - maximum size of a remotely
  referenced policy document fetched through `PolicyReference`.
  Default: `67108864` bytes (`64 MiB`).

Policy normalization also enforces a hard cap of `10000` policy alternatives.
This limit applies to the number of normalized alternatives produced by policy
normalization and intersection, and helps prevent crafted policies from
triggering exponential expansion.
