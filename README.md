# VerityFlow — Specific-User Document Verification

A Java Servlet/JSP document verification system where an Admin configures an organization, users, document types, and the exact sequential verification order for each workflow.

## Core flow

```text
Admin creates organization
        ↓
Admin creates users
        ↓
Admin creates document type
        ↓
Admin defines verification order
        ↓
Submitter uploads document
        ↓
User 1 verifies
        ↓
User 2 verifies
        ↓
User 3 verifies
        ↓
APPROVED
```

Admin is **not a verifier**. A workflow stage points directly to a specific user. Departments are not used.

## Technology

- Java 11+
- Java Servlet 4.0 / JSP
- Apache Tomcat 9
- JDBC
- **Supabase PostgreSQL**
- **Supabase Storage** for uploaded documents
- JavaScript / Fetch / AJAX
- Eclipse-compatible Dynamic Web Project

## Supabase architecture

```text
Java Servlet application
       |
       +---- JDBC ----------------> Supabase PostgreSQL
       |                             - users
       |                             - organizations
       |                             - workflows
       |                             - documents
       |                             - approvals
       |                             - audit logs
       |
       +---- HTTPS Storage API ----> Supabase Storage
                                     - private documents bucket
                                     - document versions
```

The database stores document metadata and the Storage object path. The actual PDF/DOC/DOCX file is stored in the private `documents` bucket.

## 1. Create the Supabase project

Create a Supabase project and open **SQL Editor**.

Run, in order:

```sql
-- 1

database/schema.sql

-- 2

database/demo-data.sql
```

The schema creates the application tables and a **private** Storage bucket named `documents`.

Supabase Storage files are kept outside PostgreSQL and protected by Storage access controls. The application uses a server-side secret key, so that key must never be exposed in browser code.

## 2. Configure the application

Copy `.env.example` and set:

```text
DB_URL=jdbc:postgresql://YOUR-POOLER-HOST:5432/postgres?sslmode=require
DB_USERNAME=postgres.YOUR_PROJECT_REF
DB_PASSWORD=YOUR_DATABASE_PASSWORD

SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_SECRET_KEY=YOUR_SERVER_SIDE_SECRET_KEY
SUPABASE_STORAGE_BUCKET=documents
```

For a deployed Java/Tomcat backend, use the **Supabase Session Pooler (port 5432)** connection string from the Supabase **Connect** panel. This is the IPv4-compatible pooled option and supports normal JDBC session behavior.

Supabase currently recommends using the newer server-side **secret key** where available. The code also accepts the legacy `SUPABASE_SERVICE_ROLE_KEY` variable for compatibility. Never put either key in JSP, JavaScript, HTML, or GitHub.

## 3. PostgreSQL JDBC driver

The project uses PostgreSQL JDBC **42.7.13**. If the JAR is not already present in `src/main/webapp/WEB-INF/lib`, run:

```powershell
.\download-postgresql-driver.ps1
```

or:

```text
Download the PostgreSQL JDBC driver and place postgresql-42.7.13.jar in:
src/main/webapp/WEB-INF/lib/
```

The included `pom.xml` also declares the driver for Maven builds.

## 4. Demo accounts

```text
admin@example.com     / Admin@123
submitter@example.com / Submit@123
approver@example.com  / Verify@123
finance@example.com   / Finance@123
```

Demo workflow:

```text
Purchase Request
    ↓
Demo Approver
    ↓
Demo Finance
    ↓
APPROVED
```

The Admin account is intentionally not assigned as a verifier.

## 5. File storage

Uploaded files are **not saved to the Tomcat server filesystem**.

They are stored as:

```text
Supabase Storage
└── documents
    └── organizations/{organizationId}
        └── documents/{documentId}
            └── v{version}
                └── generated-file-name.pdf
```

The database stores this object path in `document_versions.file_path`.

When a user downloads a document, the Java backend retrieves it from Supabase Storage and streams it to the browser.

## 6. Build / Eclipse

Import as an existing Eclipse project:

`File → Import → General → Existing Projects into Workspace`

Use:

- JDK 11 or newer
- Apache Tomcat 9
- Dynamic Web Module 4.0

Run `download-postgresql-driver.ps1` once before launching from Eclipse if the PostgreSQL JDBC JAR is missing.

## 7. Maven build

```powershell
mvn clean package
```

The resulting WAR is:

```text
target/verityflow.war
```

## 8. Deployment

The application can run on a Java/Tomcat host/container. Set the five required runtime values in the deployment environment:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SUPABASE_URL
SUPABASE_SECRET_KEY
```

No local database or local document directory is required.

## Security

- Admin cannot approve documents.
- Only the exact user assigned to the current workflow stage can approve/reject/request changes.
- Organization boundaries are enforced server-side.
- Uploaded documents are stored in a private Supabase Storage bucket.
- File names are randomized.
- PDF/DOC/DOCX content is validated.
- Maximum file size is 10 MB.
- CSRF protection is retained.
- Passwords are stored as BCrypt hashes.
- Supabase secret/service keys must remain server-side.

## UI changes

- Removed the old Administration Modules / Tenant Admin Scope block.
- No Departments module.
- Audit Log filter buttons work together with search.
- Added `← Back` and `↑` scroll-to-top controls.
