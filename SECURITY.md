# Security Policy

## Supported Versions

We actively maintain and provide security updates for the following versions of CoreCmp:

| Version | Supported |
|--------|-----------|
| 1.x.x  | yes |
| 0.x.x  | no |

If you are using an unsupported version, we strongly recommend upgrading to the latest release.

---

## Reporting a Vulnerability

If you discover a security vulnerability in CoreCmp, please report it responsibly.

Do NOT create a public GitHub issue for security vulnerabilities.

Instead, report privately via email:

ajay.swami.dev@gmail.com

Please include as much information as possible:

- description of the vulnerability
- steps to reproduce
- affected version
- possible impact
- proof of concept (if available)
- screenshots or logs (if relevant)

We aim to respond within:

48 hours

---

## Scope

Security vulnerabilities may include:

- sensitive data exposure
- token leakage
- improper authentication handling
- insecure storage of credentials
- unsafe file handling
- dependency vulnerabilities
- network security issues
- serialization vulnerabilities

---

## Responsible Disclosure

We request that you:

- allow time for the issue to be investigated and resolved
- avoid publicly disclosing the vulnerability until a fix is released
- provide clear reproduction details

We appreciate responsible disclosure and will acknowledge contributors who help improve the security of the project.

---

## Security Best Practices for Users

When using CoreCmp:

- do not store sensitive secrets directly in code
- use secure storage for tokens when possible
- validate file size before upload
- avoid logging sensitive data
- keep dependencies updated
- use HTTPS APIs only
- restrict file upload types if required

---

## Dependency Security

We monitor dependencies for known vulnerabilities and update them periodically.

Developers are encouraged to:

run dependency updates regularly

example:

./gradlew dependencyUpdates

---

## Security Updates

Security fixes will be released as patch versions:

example:

1.0.1
1.0.2

---

## Contact

Maintainer:

Ajay Swami

Email:

pqopqopqo000@gmail.com