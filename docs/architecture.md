# Architecture

```text
Browser
   |
   v
JSP + JavaScript
   |
   v
Java Servlets
   |
   +---------------- JDBC ----------------> Supabase PostgreSQL
   |                                          |
   |                                          +-- organizations
   |                                          +-- users / roles
   |                                          +-- document_types
   |                                          +-- workflows / workflow_stages
   |                                          +-- documents / versions
   |                                          +-- approvals / notifications
   |                                          +-- audit_logs
   |
   +------------- HTTPS Storage API -------> Supabase Storage
                                              |
                                              +-- private documents bucket
```

The workflow is **specific-user sequential verification**. Each workflow stage references a concrete `user_id`; departments are not part of routing. Admin configures the workflow but is not allowed to verify documents.

Uploaded files are stored in the private Supabase Storage bucket `documents`. PostgreSQL stores only metadata and the Storage object path. The Java backend uses the Supabase server-side secret key for Storage operations; the key is never exposed to the browser.
