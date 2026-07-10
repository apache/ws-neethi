<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Apache Neethi Security Threat Model (draft)

## §1 Header

- **Project**: Apache Neethi — *"an implementation of WS-Policy
  Specification (September, 2007) which can be located at:
  https://www.w3.org/TR/2007/REC-ws-policy-20070904/"*. *"It provides a
  convenient model and an API to process policy information at runtime
  and an extension model for serialization and de-serialization of
  domain-specific Assertions"* *(documented: `README.txt`)*.
- **Repository**: `apache/ws-neethi`.
- **Version / commit**: this model is drafted against the default branch
  at clone time. A report against project release *N* should be triaged
  against the model as it stood at *N*, not at HEAD. Latest release
  documented in `RELEASE-NOTE.txt`.
- **Date**: 2026-05-30.
- **Authors**: ASF Security team draft, awaiting Neethi / Webservices
  PMC review.
- **Status**: draft — under maintainer review.
- **Reporting**: vulnerabilities that fall under §8 (claimed
  properties) should be reported per the Apache Security Team disclosure
  channel (<https://www.apache.org/security/>); reports that fall under
  §3 (out of scope) or §9 (properties not provided) will be closed by
  Neethi triagers citing this document. The project does not ship an
  in-repo `SECURITY.md` at draft time *(inferred — §14 Q1)*.
- **Provenance legend** —
  *(documented)* = drawn from in-repo docs / source comments / project
  website with citation;
  *(maintainer)* = stated by a Neethi maintainer in response to this
  draft;
  *(inferred)* = synthesized by the producer from code structure or
  domain knowledge, awaiting PMC ratification (every *(inferred)* tag has
  a matching §14 question).
- **Draft confidence**: 18 documented / 0 maintainer / 22 inferred.

Neethi is a small Java library — a few dozen classes under
`org.apache.neethi` — that builds an in-memory tree of WS-Policy /
WS-PolicyAttachment policy assertions, normalizes (`normalize()`),
intersects (`util.PolicyIntersector`), merges, and serializes them. It
is consumed primarily by SOAP stacks (Apache CXF, Apache Axis2) and by
related projects like Apache WSS4J's `ws-security-policy-stax` to drive
WS-SecurityPolicy enforcement. Neethi is *not* the policy enforcer; it
is the policy-document parser and the policy-algebra evaluator.

## §2 Scope and intended use

### Intended use

- In-process parsing and manipulation of WS-Policy XML documents into a
  Java object model (`Policy`, `All`, `ExactlyOne`, `Assertion`,
  `PolicyReference`, `PolicyComponent`); deserialization from an
  `InputStream`, `XMLStreamReader`, Axiom `OMElement`, or W3C
  `Element`; serialization back via `XMLStreamWriter`; policy
  intersection (`PolicyIntersector`) and equivalence
  (`PolicyComparator`) over assertion trees *(documented: `README.txt`,
  the public API in `org.apache.neethi`)*.
- Two API entry points: `PolicyEngine.getPolicy(...)` (static convenience
  facade) and `PolicyBuilder.getPolicy(...)` (per-instance, allows custom
  `AssertionBuilderFactory` registration).
- An extension mechanism — `AssertionBuilder` instances registered
  against `QName`s — lets domain-specific assertion vocabularies (e.g.
  WS-SecurityPolicy, WS-AddressingPolicy, WS-ReliableMessagingPolicy)
  plug in their own assertion model classes
  *(documented: `org.apache.neethi.AssertionBuilderFactory`,
  `AssertionBuilderFactoryImpl`)*.

### Deployment shape

Neethi is an in-process Java library. The threat model is therefore
that of an **XML-parsing library** with one prominent additional
characteristic: **`PolicyReference.normalize()` issues a synchronous
HTTP(S) GET to dereference policy references that are not already
registered locally** *(documented:
`src/main/java/org/apache/neethi/PolicyReference.java` lines 141-190)*.

### Caller roles

| Role | Trust level | Notes |
| --- | --- | --- |
| **Embedding Java application / SOAP stack** | trusted | Constructs `PolicyBuilder`, supplies the policy bytes, holds the `PolicyRegistry`, drives `Policy.normalize(...)` (which may trigger `PolicyReference.getRemoteReferencedPolicy(...)`). |
| **`AssertionBuilder` implementation** | trusted | Registered against a `QName`; converts an `OMElement` / `Element` / `XMLStreamReader` into a domain-specific `Assertion`. A hostile builder can return whatever model object it wants. |
| **`PolicyRegistry` implementation** | trusted | The lookup table consulted by `PolicyReference.normalize(...)` before any network fetch is attempted. |
| **Producer of the policy bytes** | **variable** — see §6 trust table | `InputStream`, `XMLStreamReader`, `OMElement`, `Element`. Some entry points harden the parser; others trust the caller. |
| **HTTP(S) endpoint named by an absolute `PolicyReference` `URI`** | **untrusted; bounded by Neethi's bundled SSRF filter** | `PolicyReference.getRemoteReferencedPolicy(...)` fetches whatever the URL returns; this is parsed as a Policy by recursive call. The filter rejects link-local, multicast, and any-local addresses but **permits loopback and RFC-1918 / site-local addresses** *(documented: `PolicyReference.java` lines 154-165)*. |
| **Apache Axiom on the classpath** | trusted upstream | Neethi consumes the Axiom OM for the `OMElement` overloads. Defects in Axiom are out of model. |

### Component-family table

| Family | Representative entry point | Touches outside the process? | In-model? |
| --- | --- | --- | --- |
| Policy parser (`PolicyEngine`, `PolicyBuilder`) | `PolicyEngine.getPolicy(InputStream)` | **no** for in-memory model; **yes** when a `PolicyReference` is normalized | **yes** |
| Policy normalizer / intersector / comparator (`Policy`, `All`, `ExactlyOne`, `PolicyOperator`, `util.PolicyIntersector`, `util.PolicyComparator`) | `Policy.normalize(reg, deep)`, `intersect(Policy)`, `equals(Object)` | **no** | **yes** |
| Remote `PolicyReference` resolver (`PolicyReference.getRemoteReferencedPolicy`) | implicit via `Policy.normalize(reg, deep)` when a `PolicyReference` is unresolved in the registry | **yes — HTTP/HTTPS GET** | **yes** |
| Assertion builders — `builders/`, `builders/converters/`, `builders/xml/XmlPrimitiveAssertion` | `AssertionBuilder.build(element, factory)` | **no** | **yes** |
| Service loader — `org.apache.neethi.util.Service` reads `META-INF/services/...` | reads classpath at startup | filesystem (classpath) | **yes** |
| `etc/`, `src/test/` | resource files / unit tests | n/a | **out of model** *(§3)* |

A finding is in-model only if it reaches a row marked **yes**.

## §3 Out of scope (explicit non-goals)

1. **The SOAP stack itself.** Neethi parses WS-Policy documents; it is
   the embedding stack (CXF / Axis2) that *enforces* the policy on
   inbound / outbound messages. Findings about enforcement bypass live
   with the enforcer. → `OUT-OF-MODEL: out-of-layer`.
2. **The XML parser bytes-to-DOM step when the caller passes a
   pre-parsed `OMElement` / `Element` / `XMLStreamReader`.** Neethi
   does not re-parse; the caller's `XMLInputFactory` /
   `DocumentBuilderFactory` decided the XXE / DTD posture *(inferred —
   §14 Q2)*. → `OUT-OF-MODEL: trusted-input`.
3. **Apache Axiom defects.** Reported upstream to `ws-axiom`. →
   `OUT-OF-MODEL: unsupported-component`.
4. **A network-fetched policy document's *contents*.** Once Neethi has
   fetched a remote policy via `getRemoteReferencedPolicy(...)`, the
   bytes are then parsed by the same `PolicyBuilder.getPolicy(in)` that
   would have parsed a local file — so the bytes themselves are in
   scope, but the question "was the remote host honest?" is not. The
   §7 model treats the dereferenced peer as untrusted-but-bounded by
   the address-class filter. *(inferred — §14 Q3)*. →
   `OUT-OF-MODEL: adversary-not-in-scope` for the remote host's
   *intent*; in-model for the bytes it returns.
5. **The `PolicyRegistry` implementation.** Caller-supplied; the
   contents of the registry are trusted by construction.
6. **Code shipped in `src/test/`, `etc/`.** Tests and supporting
   resources. → `OUT-OF-MODEL: unsupported-component`.

## §4 Trust boundaries and data flow

| # | Transition | Authentication | Authorization |
| --- | --- | --- | --- |
| B1 | Caller → `PolicyEngine.getPolicy(InputStream)` / `PolicyBuilder.getPolicy(InputStream)` | none | none |
| B2 | Caller → `PolicyEngine.getPolicy(OMElement | Object)` / `PolicyBuilder.getPolicy(Element)` / `.getPolicy(XMLStreamReader)` | none | none |
| B3 | Parser → `XMLInputFactory` configured by Neethi (sets `IS_SUPPORTING_EXTERNAL_ENTITIES=false`, `SUPPORT_DTD=false`) — for the `InputStream` overload only *(documented: `PolicyBuilder.java` lines 99-100, 140-141)* | none | DTD/entity gate applies on the `InputStream` overload only |
| B4 | `Policy.normalize(PolicyRegistry, deep)` → `PolicyReference.normalize(reg, deep)` → `PolicyRegistry.lookup(URI)` | none | registry is trusted |
| B5 | `PolicyReference.normalize(...)` (lookup miss) → `PolicyReference.getRemoteReferencedPolicy(uri)` → `new URL(uri)` → `URLConnection.openConnection()` → HTTP/HTTPS GET → recursive `getPolicy(in)` | none | **address-class filter**: rejects link-local, multicast, any-local; permits loopback + RFC-1918 / site-local + public *(documented: `PolicyReference.java` lines 149-168)* |
| B6 | `AssertionBuilderFactoryImpl` → `META-INF/services/org.apache.neethi.builders.AssertionBuilder` (via `util.Service.loadServiceClass(...)`) | trusted classpath | startup-time class loading |

### Reachability preconditions per family

- **`PolicyBuilder.getPolicy(InputStream)` and
  `PolicyBuilder.getPolicyReference(InputStream)`**: in-model when
  bytes are attacker-controlled. The internal `XMLInputFactory`
  disables DTD and external entities *(documented: `PolicyBuilder.java`
  lines 99-100, 140-141)*.
- **`PolicyBuilder.getPolicy(Element)`, `getPolicy(XMLStreamReader)`,
  `getPolicy(Object)` (Axiom `OMElement` overload)**: out-of-model for
  XXE — caller provided the parsed input.
- **`Policy.normalize(reg, deep)` issuing a remote fetch**: in-model
  when the original policy bytes were attacker-controllable and contain
  an unresolved `<wsp:PolicyReference URI="…"/>`. The SSRF filter is
  the §7 boundary.
- **`AssertionBuilder` plug-ins**: in-model insofar as the registered
  builder is part of this repo (none ship for security-sensitive
  domains in `apache/ws-neethi`); for downstream builders (WSS4J's
  `ws-security-policy` module), the threat model lives with the
  consumer.

## §5 Assumptions about the environment

- **JDK**: a modern JDK; specific minimum version per branch
  *(inferred — §14 Q4)*.
- **Apache Axiom** on the classpath for the `OMElement` API path.
- **`META-INF/services/org.apache.neethi.builders.AssertionBuilder`** —
  Neethi's `AssertionBuilderFactoryImpl` discovers `AssertionBuilder`
  implementations via the JDK ServiceLoader-shaped helper at
  `org.apache.neethi.util.Service`. Whatever is on the classpath is
  loaded *(documented: `org.apache.neethi.AssertionBuilderFactoryImpl`,
  `util.Service`)*.
- **Network**: `PolicyReference.getRemoteReferencedPolicy` opens an
  HTTP/HTTPS connection via the JDK URL handler with documented
  timeouts (`connectTimeout=5000`, `readTimeout=10000`,
  `setInstanceFollowRedirects(false)`) *(documented:
  `PolicyReference.java` lines 170-175)*.
- **DNS**: the remote-policy fetcher calls `InetAddress.getByName(host)`
  to perform the address-class check *(documented:
  `PolicyReference.java` line 161)*. Compromised DNS can spoof the
  address class.
- **No filesystem writes**: Neethi serializes to caller-supplied
  `XMLStreamWriter`; does not write anywhere of its own initiative
  *(inferred — §14 Q5)*.

### What Neethi does *not* do to its host (negative claims, awaiting maintainer ratification)

- Opens **no** listening sockets *(inferred — §14 Q5)*.
- Spawns **no** child processes *(inferred — §14 Q5)*.
- Installs **no** signal handlers *(inferred — §14 Q5)*.
- Reads **no** environment variables for security-sensitive choices
  *(inferred — §14 Q5)*.
- Writes only via the caller-supplied `XMLStreamWriter`
  *(inferred — §14 Q5)*.

## §5a Build-time and configuration variants

Neethi is a single Maven artifact. There are **no compile-time
feature toggles**. The runtime security envelope is shaped by:

| Knob | Default | Maintainer stance | Effect |
| --- | --- | --- | --- |
| `XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES` | `false` *(documented: `PolicyBuilder.java` lines 99, 140)* | hardened-by-default | applies only to the `InputStream` overload |
| `XMLInputFactory.SUPPORT_DTD` | `false` *(documented: `PolicyBuilder.java` lines 100, 141)* | hardened-by-default | applies only to the `InputStream` overload |
| `PolicyReference.getRemoteReferencedPolicy` — address-class filter | rejects link-local, multicast, any-local; **permits** loopback, RFC-1918 / site-local, public *(documented: `PolicyReference.java` lines 149-168)* | **maintainer ruling required** — see §14 Q6 | controls whether SSRF against loopback (e.g. localhost IMDS proxy) or RFC-1918 (internal microservices) is possible |
| `PolicyReference.getRemoteReferencedPolicy` — connect timeout | `5000` ms *(documented: `PolicyReference.java` line 173)* | hardened-by-default | bound on per-fetch connect time |
| `PolicyReference.getRemoteReferencedPolicy` — read timeout | `10000` ms *(documented: `PolicyReference.java` line 174)* | hardened-by-default | bound on per-fetch read time |
| `PolicyReference.getRemoteReferencedPolicy` — redirects | follow disabled (`setInstanceFollowRedirects(false)`) *(documented: `PolicyReference.java` line 175)* | hardened-by-default | prevents redirect-based filter bypass |
| `PolicyReference.getRemoteReferencedPolicy` — supported schemes | `http`, `https` only — others rejected *(documented: `PolicyReference.java` lines 149-152)* | hardened-by-default | blocks `file:`, `jar:`, `ftp:`, etc. |
| `META-INF/services/.../AssertionBuilder` | classpath-discovered | classpath is the trust gate | adds domain-specific assertion vocabularies |

### The insecure-default case

The remote-policy fetcher is the most interesting design choice. The
defaults are **deliberately partially open**: loopback and RFC-1918
addresses **are allowed** because the documented intent is *"so that
policies on localhost or an internal network can be resolved"*
*(documented: `PolicyReference.java` lines 158-159)*. The maintainer
ruling captured in §14 Q6 will determine whether a report shaped "an
attacker schema's `<wsp:PolicyReference URI='http://127.0.0.1:11211/' />`
reached the local memcached and exfiltrated data" is:

- (a) `VALID-HARDENING` — because the operator-trusted-internal-network
  default is *deliberately* permissive and operators are expected to
  restrict if their threat model demands; the project may tighten in
  a future release.
- (b) `OUT-OF-MODEL: by-design` — because the project's documented
  position is that loopback / RFC-1918 are trusted by construction.

The XXE / DTD posture, in contrast, is **closed**: the `InputStream`
overload disables both. The pre-parsed-`Element` / `XMLStreamReader` /
`OMElement` overloads rely on the caller's parser, by design.

## §6 Assumptions about inputs

### Per-entry-point trust table

| Entry point | Parameter | Attacker-controllable? | Caller must enforce |
| --- | --- | --- | --- |
| `PolicyEngine.getPolicy(InputStream)` / `PolicyBuilder.getPolicy(InputStream)` | bytes | **yes** | nothing for XXE/DTD — Neethi sets DTD/entity to false; bound the maximum input size externally |
| `PolicyBuilder.getPolicy(Element)` | DOM element | **yes if from untrusted bytes** | caller's `DocumentBuilderFactory` is responsible for XXE / DTD posture |
| `PolicyBuilder.getPolicy(XMLStreamReader)` | events | **yes if from untrusted bytes** | caller's `XMLInputFactory` is responsible for XXE / DTD posture |
| `PolicyEngine.getPolicy(OMElement)` / `PolicyBuilder.getPolicy(Object)` (Axiom `OMElement`) | OM | **yes if from untrusted bytes** | Axiom's `StAXParserConfiguration` is the gate (see Axiom threat model) |
| `PolicyEngine.getPolicyReferene(InputStream)` (sic — typo in upstream) / `PolicyBuilder.getPolicyReference(InputStream)` | bytes | **yes** | same as `getPolicy(InputStream)` |
| `Policy.normalize(PolicyRegistry registry, boolean deep)` | the policy + the registry | as untrusted as the policy bytes; registry is trusted | none |
| `PolicyReference.normalize(registry, deep)` | URI inside the reference | **yes** — attacker-controllable URI in the policy bytes | **NO** — Neethi does the SSRF filter; *but the filter permits loopback + RFC-1918*, so operators that consider those addresses untrusted **must** install a restricting `PolicyRegistry` that resolves all references locally and refuses to fall through to `getRemoteReferencedPolicy` |
| `AssertionBuilder.build(element, factory)` (caller-registered) | element | as untrusted as input | builder author's responsibility |
| `PolicyBuilder` ctor with `AssertionBuilderFactory` | factory | trusted | caller's choice |

### Size / shape / rate

- No documented bound on policy-document size *(inferred — §14 Q7)*.
- No documented bound on the number of `PolicyReference` URIs in a
  single policy or in a transitive resolution chain
  *(inferred — §14 Q7)*.
- No rate limit on `getRemoteReferencedPolicy` fetches — each
  unresolved `PolicyReference` triggers an HTTP GET on the spot.
- Connect-timeout (5 s) and read-timeout (10 s) bound the wall-clock
  per fetch, but a malicious policy with many distinct unresolved
  references can multiply the latency *(inferred — §14 Q7)*.

## §7 Adversary model

### Actors

| Actor | In scope? | Capabilities |
| --- | --- | --- |
| **Producer of the policy bytes** | **yes** | full byte control of the policy; can include `<wsp:PolicyReference URI='…'/>` to any HTTP/HTTPS URL |
| **Producer of an HTTP/HTTPS response** at a `PolicyReference URI` | **partial** — bounded by the address-class filter | controls the response bytes, which are then parsed as a Policy by Neethi |
| **DNS attacker** | **in scope only for the filter-bypass case** — `InetAddress.getByName(host)` is consulted once; DNS rebinding between that lookup and the JDK's connect could let an external hostname resolve to a loopback / RFC-1918 address that *the filter would have rejected* *(inferred — §14 Q8)* |
| **Network attacker on the path between Neethi and an HTTP `PolicyReference URI`** | covered by TLS when `https://` is used; for plain `http://` the operator owns the network |
| **Owner of the JVM classpath** | **out of scope** — controls `META-INF/services/` *(§3 item 5, §5)* |
| **In-process caller** | **out of scope** |
| **Co-tenant in shared JVM** | **out of scope** *(inferred — §14 Q9)* |
| **Side-channel observer** | **out of scope** *(inferred — §14 Q9)* |
| **Quantum adversary** | **out of scope** |

## §8 Security properties the project provides

### P1 — XXE / DTD disabled on the `InputStream` parse paths

- **Condition**: caller used `PolicyEngine.getPolicy(InputStream)`,
  `PolicyEngine.getPolicyReferene(InputStream)`,
  `PolicyBuilder.getPolicy(InputStream)`, or
  `PolicyBuilder.getPolicyReference(InputStream)`.
- **Violation symptom**: an external entity reference in the policy
  bytes triggers a fetch (XXE), or a quadratic / billion-laughs
  expansion completes without exception (DTD-driven).
- **Severity**: **security-critical**, `VALID` per §13.
- *(documented: `PolicyBuilder.java` lines 99-100 (`getPolicy`),
  140-141 (`getPolicyReference`))*

### P2 — Address-class-filtered remote `PolicyReference` resolution

- **Condition**: a policy contains an absolute `<wsp:PolicyReference URI='…'/>`;
  `Policy.normalize(...)` is called; the registry does not satisfy
  the reference; `PolicyReference.getRemoteReferencedPolicy(uri)` is
  reached.
- **Violation symptom**: an HTTP fetch to a *link-local* (e.g.
  `169.254.0.0/16`, `fe80::/10`), *multicast*, or *any-local* (`0.0.0.0`,
  `::`) address is reached. The published filter rejects these.
- **Severity**: **security-critical** for the cloud-IMDS case
  (`169.254.169.254`), `VALID` per §13.
- *(documented: `PolicyReference.java` lines 149-168)*

### P3 — Scheme restriction on remote `PolicyReference` URIs

- **Condition**: same as P2.
- **Violation symptom**: a non-`http` / non-`https` URI (`file:`,
  `jar:`, `ftp:`, `data:`, etc.) is dereferenced.
- **Severity**: **security-critical**, `VALID` per §13.
- *(documented: `PolicyReference.java` lines 149-152 — "Unsupported
  URI scheme: only http and https are permitted")*

### P4 — Redirects not followed on remote `PolicyReference` resolution

- **Condition**: same as P2; HTTP server returns 3xx redirect.
- **Violation symptom**: Neethi follows the redirect to a host the
  filter would have rejected.
- **Severity**: **security-critical** (filter-bypass primitive),
  `VALID` per §13.
- *(documented: `PolicyReference.java` line 175 —
  `setInstanceFollowRedirects(false)`)*

### P5 — Per-fetch connect-timeout and read-timeout on remote `PolicyReference` resolution

- **Condition**: same as P2; the named server hangs or stalls.
- **Violation symptom**: `Policy.normalize(...)` blocks for more than
  ~15 seconds per reference.
- **Severity**: **availability-relevant**; `VALID-HARDENING`
  *(inferred — §14 Q7)* — wall-clock bound but no bound on the
  *number* of references resolved per policy.
- *(documented: `PolicyReference.java` lines 173-174)*

### P6 — Policy intersection / equivalence is total: any two well-formed `Policy` objects can be compared

- **Condition**: both `Policy` objects are well-formed.
- **Violation symptom**: `PolicyIntersector.intersect(p1, p2)` throws
  an unhandled exception or returns a structurally invalid `Policy`.
- **Severity**: **correctness-only**, `VALID-HARDENING` if the
  divergence is security-meaningful downstream.
- *(inferred — §14 Q10)*

## §9 Security properties the project does *not* provide

State each plainly so a triager can route an inbound report to the
matching disclaimer.

- **No defense against attacker control of the policy bytes themselves
  when the caller passes a pre-parsed `Element` / `XMLStreamReader` /
  `OMElement`.** The XXE/DTD hardening is only on the `InputStream`
  overloads *(documented: `PolicyBuilder.java`)*.
- **No defense against an attacker fetch to a loopback address.**
  The remote-policy fetcher *permits* loopback by documented
  intent ("policies on localhost or an internal network can be
  resolved") *(documented: `PolicyReference.java` lines 158-159)*. An
  attacker can drive a policy whose `<wsp:PolicyReference URI='http://127.0.0.1:8500/'/>`
  reaches a local-bound Consul / memcached / Redis / etcd HTTP endpoint.
- **No defense against an attacker fetch to an RFC-1918 / site-local
  address.** Same documented intent: internal networks are within
  reach *(documented: `PolicyReference.java` lines 158-159)*.
- **No defense against DNS rebinding between the filter check and the
  JDK connect call.** The filter resolves the host once; the JDK's
  `URLConnection.connect` may resolve it again *(inferred —
  §14 Q8)*.
- **No bound on the number of `PolicyReference` URIs Neethi will fetch
  during a single `Policy.normalize(...)` call.** A policy with 1000
  unresolved references will issue 1000 sequential HTTP GETs,
  bounded only by the per-fetch timeouts (5+10 s each).
- **No defense against an attacker who controls a remote policy
  endpoint** (within the address-class filter): the bytes returned
  are parsed as a Policy. Once parsed, that Policy can itself have a
  `PolicyReference` to yet another URL, leading to a transitive fetch
  chain. There is **no documented limit on fetch chain depth**
  *(inferred — §14 Q11)*.
- **No data-at-rest protection.** Serialized policies are XML on the
  sink the caller provided.
- **No protection of the `PolicyRegistry` contents.** A registry
  with a poisoned entry (an attacker-controlled `Policy` registered
  against a well-known URI) is the embedding application's bug.
- **No defense against a hostile `AssertionBuilder`** on the
  classpath.
- **No constant-time guarantees** (Neethi does not deal with
  secrets).
- **No defense against side-channel observation.**
- **No quantum resistance.**

### False-friend properties (call out separately)

- **The address-class filter looks like an SSRF firewall, but it
  isn't.** It is a deliberately partial sieve: loopback and RFC-1918
  are permitted. Operators whose threat model includes "policy bytes
  from external clients reaching internal HTTP services" must restrict
  via the `PolicyRegistry` *(documented: `PolicyReference.java` lines
  158-159)*.
- **`setInstanceFollowRedirects(false)` looks like it makes the
  filter complete, but DNS rebinding between the address check and
  the JDK connect can still pivot.** *(inferred — §14 Q8)*.
- **`XMLInputFactory.SUPPORT_DTD=false` on the `InputStream` overload
  looks like a universal XXE defense, but the `Element` /
  `XMLStreamReader` / `OMElement` overloads do **not** apply it.**
  Caller is responsible.
- **`PolicyIntersector.intersect(p1, p2)` looks like authorization
  logic, but it isn't.** It is a policy-algebra operation; whether
  the resulting `Policy` is *enforceable* is the SOAP stack's
  concern.
- **`PolicyEngine.getPolicy(...)` is a static facade with mutable
  shared state.** A `static synchronized PolicyBuilder` is reused
  across calls; an `AssertionBuilder` registered on one call sticks
  around *(documented: `PolicyEngine.java` lines 45-52)*. Surprises
  arise in tests or in JVMs with mixed-trust callers.

### Well-known attack classes Neethi does not single-handedly defend against

- **SSRF to loopback / RFC-1918** via attacker-crafted policy bytes —
  *(see §9 above)*.
- **DNS-rebinding bypass of the address-class filter** *(inferred —
  §14 Q8)*.
- **Resource exhaustion via many `PolicyReference` URIs** in one
  policy.
- **XXE / billion-laughs** when the caller passes a pre-parsed
  `Element` / `OMElement` / `XMLStreamReader` that came from an
  un-hardened parser.
- **Confused-deputy via `getRemoteReferencedPolicy`**: Neethi as a
  proxy issuing internal HTTP from a host whose source-IP is
  privileged.

## §10 Downstream responsibilities

The embedding Java application **must**:

1. For attacker-controlled policy bytes coming in as a pre-parsed
   `OMElement` / `Element` / `XMLStreamReader`, harden the upstream
   parser:
     - For Axiom: use `StAXParserConfiguration.SOAP` or `STANDALONE`
       per the Axiom threat model.
     - For `DocumentBuilderFactory`: set `disallow-doctype-decl=true`
       and external entities to false.
     - For `XMLInputFactory`: set `SUPPORT_DTD=false` and
       `IS_SUPPORTING_EXTERNAL_ENTITIES=false`.
2. For *any* deployment where the policy bytes may originate from an
   untrusted peer (e.g. a WS-Policy advertised by a service the
   embedder consumes), **populate the `PolicyRegistry` ahead of time
   with all expected policy references**, so that
   `PolicyReference.normalize(registry, deep)` resolves locally and
   the remote-fetch path is never reached.
3. If the deployment's threat model treats loopback or RFC-1918 as
   untrusted (e.g. multi-tenant cloud), install a custom
   `PolicyRegistry` that always satisfies references locally **or**
   filter policy bytes for absolute-URI `PolicyReference` elements
   before handing them to Neethi.
4. Cap the policy-document size and the number of `PolicyReference`
   URIs at the *caller* layer. Neethi imposes no such bound
   *(inferred — §14 Q7)*.
5. Prefer `https://` policy URIs for any references that survive into
   the remote-fetch path. The address-class filter does not enforce
   scheme-vs-host pairing.
6. Lock down `META-INF/services/org.apache.neethi.builders.AssertionBuilder`
   contents at packaging time; a hostile builder on the classpath can
   substitute model objects.
7. Treat the `PolicyEngine` static facade with caution in shared-state
   JVMs — `AssertionBuilder` registration is process-wide.

## §11 Known misuse patterns

- **Letting attacker bytes reach `PolicyEngine.getPolicy(Element)` or
  `getPolicy(OMElement)` without an XXE-hardened upstream parser.**
- **Treating the address-class filter as a complete SSRF defense.**
  Loopback and RFC-1918 are reachable by design.
- **Calling `Policy.normalize(registry, deep)` on a policy parsed
  from untrusted bytes without a fully-populated registry.** The
  fall-through path to `getRemoteReferencedPolicy` is the attack
  surface.
- **Registering `AssertionBuilder` instances on the `PolicyEngine`
  static facade in a multi-tenant JVM.** Registration persists.
- **Using `http://` for policy references in production.** A network
  attacker can substitute.
- **Trusting `PolicyIntersector.intersect` to be commutative or
  associative without inspecting the model spec carefully.**
- **Passing the same `PolicyBuilder` across threads without external
  synchronization.** Threading model unspecified *(inferred —
  §14 Q12)*.
- **Storing policies in a `PolicyRegistry` that an attacker can
  poison via a separate code path.**

## §11a Known non-findings (recurring false positives)

This section is the highest-leverage input for automated agentic
security scans. Each entry: tool symptom, why it is safe under the
model, the section that licenses the call.

- **"`URLConnection.getInputStream()` is SSRF."** Neethi's
  `PolicyReference.getRemoteReferencedPolicy` is the policy-deref
  fetcher. The address-class filter and the scheme restriction are the
  in-source guards *(documented: `PolicyReference.java` lines 149-175)*.
  Reports that loopback / RFC-1918 are reachable are
  *(inferred — §14 Q6)*: `BY-DESIGN: property-disclaimed` if the
  documented intent stands; `VALID-HARDENING` if the project tightens.
- **"`DocumentBuilderFactory.newInstance()` not hardened."** Neethi
  does not use `DocumentBuilderFactory`. → `KNOWN-NON-FINDING`.
- **"`XMLInputFactory.newInstance()` not hardened."** Check the line
  — it is *(documented: `PolicyBuilder.java` lines 99-100, 140-141)*.
  → `KNOWN-NON-FINDING` for the `InputStream` paths.
- **"`InetAddress.getByName(...)` is vulnerable to DNS rebinding."**
  Acknowledged in §9; the filter is best-effort *(inferred —
  §14 Q8)*. → `VALID-HARDENING` *only* if the maintainer rules at
  Q8 that DNS-rebinding tightening is in scope; otherwise
  `BY-DESIGN: property-disclaimed`.
- **"Static `PolicyBuilder` in `PolicyEngine` is global mutable
  state."** Documented facade; intentional. → `KNOWN-NON-FINDING`.
- **"`Class.forName` via `org.apache.neethi.util.Service`."** ServiceLoader-
  shaped helper; classpath is the trust gate. → `OUT-OF-MODEL:
  trusted-input`.
- **"`HttpURLConnection.setInstanceFollowRedirects(false)` cast may
  ClassCastException for `file:` URLs."** Scheme restriction rejects
  non-HTTP/HTTPS upstream *(documented: `PolicyReference.java` lines
  149-152)*. → `KNOWN-NON-FINDING`.
- **"`RuntimeException` thrown from `getPolicy(...)` on malformed
  XML."** Documented failure mode. → `KNOWN-NON-FINDING`.
- **"`InputStream` not closed in finally."** `in.close()` is in
  `finally`-style `try/finally` block *(documented: `PolicyReference.java`
  lines 178-186)*. → `KNOWN-NON-FINDING`.
- **"`opensaml-*.jar` has CVE-X."** Neethi does not depend on
  OpenSAML. → `OUT-OF-MODEL: unsupported-component`.
- **"`PolicyComparator.compare(...)` is non-deterministic."** Confirm
  visit-order semantics *(inferred — §14 Q10)*. → `MODEL-GAP`
  pending confirmation.

## §12 Conditions that would change this model

Revise this document when any of the following lands:

- A change in the address-class filter in
  `PolicyReference.getRemoteReferencedPolicy` — e.g. blocking loopback
  / RFC-1918 by default, or adding DNS-rebinding mitigation.
- A change in the supported URI schemes for remote-policy fetch.
- A change in the connect / read timeout defaults.
- A new policy-document entry point on `PolicyBuilder` or
  `PolicyEngine`.
- A new public API for fetching a `Policy` from a network source other
  than via `PolicyReference`.
- A bound on the maximum number of `PolicyReference` URIs resolved per
  `Policy.normalize(...)`.
- A change in the `AssertionBuilder` discovery mechanism (e.g. JDK
  ServiceLoader instead of `util.Service`).
- A vulnerability report that cannot be cleanly routed to one of the
  §13 dispositions.

## §13 Triage dispositions

A report against Neethi receives exactly one of the following:

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | Violates a §8 property via an in-scope §7 adversary using an in-scope §6 input. | §8, §6, §7 |
| `VALID-HARDENING` | No §8 property violated, but a §11 misuse pattern can be made harder to fall into by code change. Typically no CVE. | §11 |
| `OUT-OF-MODEL: trusted-input` | Requires attacker control of a §6 parameter the model marks trusted (caller-supplied `Element` / `OMElement` / `XMLStreamReader`, `PolicyRegistry` contents, custom `AssertionBuilder`). | §6 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires a §7 actor the model excludes (in-process caller, owner of the JVM classpath, hostile builder on classpath, operator). | §7 |
| `OUT-OF-MODEL: unsupported-component` | Lands in `src/test/`, `etc/`, or in upstream (Axiom). | §3 items 3, 6 |
| `OUT-OF-MODEL: non-default-build` | Only manifests under a §5a configuration the maintainer rules dev/test. *(currently none — Neethi has no per-build security knobs)* | §5a |
| `OUT-OF-MODEL: out-of-layer` | Concerns a SOAP-stack policy *enforcement* step or a higher-layer authorization concern. | §3 item 1 |
| `BY-DESIGN: property-disclaimed` | Concerns a §9 property the project explicitly does not provide (loopback / RFC-1918 SSRF, no fetch-count bound, no XXE defense on pre-parsed paths). | §9 |
| `KNOWN-NON-FINDING` | Matches a §11a recurring false positive. | §11a |
| `MODEL-GAP` | Cannot be cleanly routed to any of the above — triggers §12 model revision. | §12 |

## §14 Open questions for the maintainers

Every *(inferred)* tag in the body maps to one of these. Proposed
answers are inline; please confirm, correct, or strike.

### Wave 1 — security policy + meta

**Q1.** Neethi does not currently ship an in-repo `SECURITY.md`.
Should the project (a) adopt a `SECURITY.md` that names a supported-
branch matrix (proposed), (b) leave reporting to the foundation page
only, or (c) defer to the Webservices PMC's umbrella policy? *(meta)*

**Q2.** Confirm that the pre-parsed-`Element` / `XMLStreamReader` /
`OMElement` overloads are documented as inheriting the caller's
XXE / DTD posture (proposed: **yes**, §6 trust table, §10 item 1).
*(maps to §3 item 2)*

**Q3.** Confirm that the remote-fetched policy bytes are treated as
"untrusted bytes parsed by Neethi" but the remote *host's identity* is
trusted by §7 (proposed). *(maps to §3 item 4)*

**Q4.** Supported JDK matrix per branch (please specify minimum).
*(maps to §5)*

**Q5.** Negative-side inventory in §5: Neethi opens **no** listening
sockets, spawns **no** processes, installs **no** signal handlers,
reads **no** environment variables, and writes only via the caller-
supplied `XMLStreamWriter`. Confirm? *(maps to §5)*

### Wave 2 — SSRF filter (highest leverage)

**Q6.** **The big remote-fetch question.** The current
`PolicyReference.getRemoteReferencedPolicy` filter *permits* loopback
(`127.0.0.0/8`, `::1`) and RFC-1918 / site-local. The documented
intent is "so that policies on localhost or an internal network can
be resolved". Is this:

- (a) **Supported production posture** — i.e. a report shaped "an
  attacker policy `<wsp:PolicyReference URI='http://127.0.0.1:8500/'/>`
  reached a local-bound Consul / memcached / Redis / etcd HTTP
  endpoint" is `BY-DESIGN: property-disclaimed`?
- (b) **Documented dev/test default; operators handling untrusted
  policy bytes are required to install a fully-populated
  `PolicyRegistry` per §10 item 2** — i.e. same report is
  `OUT-OF-MODEL: trusted-input`?
- (c) **A known partial defense to be tightened in a future release**
  — same report is `VALID-HARDENING`?

Proposed answer: **(b)**, with a §10 item that documents the
expectation. *(maps to §5a, §7, §9, §10 items 2-3, §11, §11a, §13)*

**Q7.** No documented bound on policy-document size or on the number
of `PolicyReference` URIs per policy or per resolution chain
(proposed: confirm "no bound; caller imposes"). Should a future release
add a maximum-fetches-per-normalize counter? *(maps to §6, §9, §10
item 4, §11)*

**Q8.** DNS rebinding between the filter check
(`InetAddress.getByName(host)`) and the JDK `URLConnection.connect()`:
proposed treatment is "best-effort filter; rebinding is a known
partial defense; `BY-DESIGN: property-disclaimed`". Confirm, or
mark `VALID-HARDENING` for a future fix? *(maps to §7, §9, §11a)*

**Q9.** Co-tenant in shared JVM and side-channel observers: out of
scope (proposed)? *(maps to §7)*

### Wave 3 — correctness properties

**Q10.** Policy intersection / equivalence: are the documented
semantics commutative / associative / total? Proposed: total but
no algebraic-law guarantee. *(maps to §8 P6, §11)*

**Q11.** Transitive `PolicyReference` resolution chain: is there a
maximum depth in code, or could a malicious policy at remote-host-A
include a reference to remote-host-B which references remote-host-A
again? Proposed: no bound; cycle detection is the caller's
responsibility. *(maps to §9, §11)*

**Q12.** Threading model: confirm `PolicyBuilder` instances are
not thread-safe; concurrent calls to `getPolicy(...)` on the same
instance are unsupported (proposed). The `PolicyEngine` static facade
*does* use `synchronized` on `getBuilder()` so that path is safe.
*(maps to §11)*

### Wave 4 — coexistence & publication

**Q13.** This document should be hosted in-repo at
`docs/security/threat-model.md` (proposed) or on
`ws.apache.org/neethi/`? *(meta)*

**Q14.** §11a known-non-findings is thin (~11 patterns). Could the
Neethi PMC populate from JIRA "not a bug" / "wontfix" closures
(`NEETHI-*` tickets)? Concrete asks: 3–5 patterns the PMC sees recur
in inbound reports. *(meta — §11a)*

**Q15.** What kind of change to Neethi should trigger a revision
(proposed list in §12 — confirm or correct)? *(meta — §12)*

---

## Appendix: SECURITY.md / website → §x back-map

Neethi does not ship a `SECURITY.md`. The threat-model sources are
the in-repo `README.txt`, the `RELEASE-NOTE.txt`, and the JavaDoc /
source comments. The project website is
<https://ws.apache.org/neethi/>.

| Source | Claim | Lands in |
| --- | --- | --- |
| `README.txt` | "implementation of WS-Policy Specification (September, 2007)"; "It provides a convenient model and an API to process policy information at runtime and an extension model for serialization and de-serialization of domain-specific Assertions" | §1, §2 intended use |
| `src/main/java/org/apache/neethi/PolicyBuilder.java` lines 99-100 (`getPolicy(InputStream)`) | `xif.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, FALSE); xif.setProperty(SUPPORT_DTD, FALSE)` | §8 P1, §11a |
| `PolicyBuilder.java` lines 140-141 (`getPolicyReference(InputStream)`) | same XXE/DTD hardening on the PolicyReference parse path | §8 P1, §11a |
| `PolicyReference.java` lines 141-190 (`getRemoteReferencedPolicy(String u)`) | the remote-policy fetcher | §1 (deployment shape), §4 B5, §5 network, §5a, §8 P2-P5, §9 first three bullets, §10 items 2-3, §11 |
| `PolicyReference.java` lines 149-152 | "Unsupported URI scheme: only http and https are permitted" | §8 P3, §11a |
| `PolicyReference.java` lines 154-159 | "Resolve the host to an IP and reject addresses that can never serve a policy document: link-local … multicast … any-local … Loopback (127.x.x.x / ::1) and site-local (RFC-1918) addresses are permitted so that policies on localhost or an internal network can be resolved" | §5a (insecure-default case), §8 P2, §9, §14 Q6 |
| `PolicyReference.java` lines 160-168 | `InetAddress.getByName(...)` address-class check | §8 P2, §11a, §14 Q8 |
| `PolicyReference.java` lines 173-175 | `connectTimeout=5000`, `readTimeout=10000`, `setInstanceFollowRedirects(false)` | §5a, §8 P4-P5 |
| `PolicyEngine.java` lines 45-52 | "static synchronized PolicyBuilder" facade | §9 false-friend, §11 |
| `AssertionBuilderFactoryImpl.java`, `util.Service` | ServiceLoader-style discovery of `AssertionBuilder` via `META-INF/services/` | §5, §10 item 6 |
| `Policy.java`, `All.java`, `ExactlyOne.java`, `AbstractPolicyOperator.java` | `normalize(reg, deep)` triggers `PolicyReference.normalize(reg, deep)` recursively | §4 B4-B5, §11 |
| `util.PolicyIntersector`, `util.PolicyComparator` | policy-algebra utilities | §8 P6, §14 Q10 |
| `RELEASE-NOTE.txt` | release notes per version | §1 supported branches |
