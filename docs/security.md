# Security Checklist

- Passwords are stored as BCrypt hashes.
- JDBC queries use PreparedStatement.
- Organization IDs come from the server-side session.
- Session timeout is configured.
- Authentication and authorization filters protect application areas.
- Uploaded files are restricted to PDF/DOC/DOCX and a 10 MB maximum.
- Stored object names use random UUIDs.
- Uploaded documents are stored in a **private Supabase Storage bucket**.
- The Supabase secret/service key is used only by the Java backend and is never exposed to the browser.
- XML parsing disables DTDs and external entities.
- Audit logging records important security and workflow events.
- Production deployment should use HTTPS and secure cookies.
- Production secrets must be supplied through environment/server configuration, not committed to Git.
- CSRF protection is retained for state-changing requests.
- Only the exact user assigned to the current workflow stage can verify a document.
- Admin accounts are prevented from being workflow verifiers.
