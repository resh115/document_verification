# Workflow Rules

A workflow belongs to a document type and contains an ordered list of specific users.
The administrator configures the order; the administrator is not a verifier unless the application is explicitly changed to allow that role.

Example:

Submitter -> User A -> User B -> User C -> Approved

## Approve

The current assigned user completes the stage and the next configured user becomes active.

## Reject

The document becomes `REJECTED` and the workflow stops.

## Request Changes

The document becomes `CHANGES_REQUIRED`.

The submitter uploads a new version.

The workflow resumes at the exact stage that requested the changes.

Previous versions and approval actions remain in the history.
