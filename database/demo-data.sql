-- Supabase PostgreSQL demo data
-- Passwords are for demo/testing only:
-- admin@example.com     -> Admin@123
-- submitter@example.com -> Submit@123
-- approver@example.com  -> Verify@123
-- finance@example.com   -> Finance@123

DO $$
DECLARE
    v_org_id BIGINT;
    v_admin_role BIGINT;
    v_submitter_role BIGINT;
    v_approver_role BIGINT;
    v_finance_role BIGINT;
    v_approver_user BIGINT;
    v_finance_user BIGINT;
    v_doc_type BIGINT;
    v_workflow_id BIGINT;
BEGIN

    -- =========================================================
    -- ORGANIZATION
    -- =========================================================

    INSERT INTO organizations (name, code, type)
    VALUES (
        'Verity Demo Organization',
        'VERITY-DEMO',
        'Enterprise'
    )
    ON CONFLICT (code)
    DO UPDATE SET
        name = EXCLUDED.name,
        type = EXCLUDED.type
    RETURNING id INTO v_org_id;


    -- =========================================================
    -- ROLES
    -- =========================================================

    INSERT INTO roles (
        organization_id,
        name,
        description
    )
    VALUES
        (
            v_org_id,
            'ADMIN',
            'Organization administrator'
        ),
        (
            v_org_id,
            'SUBMITTER',
            'Document submitter'
        ),
        (
            v_org_id,
            'APPROVER',
            'Document verifier'
        ),
        (
            v_org_id,
            'FINANCE',
            'Finance verifier'
        )
    ON CONFLICT (organization_id, name)
    DO UPDATE SET
        description = EXCLUDED.description;


    -- Get role IDs

    SELECT r.id
    INTO v_admin_role
    FROM roles r
    WHERE r.organization_id = v_org_id
      AND r.name = 'ADMIN';

    SELECT r.id
    INTO v_submitter_role
    FROM roles r
    WHERE r.organization_id = v_org_id
      AND r.name = 'SUBMITTER';

    SELECT r.id
    INTO v_approver_role
    FROM roles r
    WHERE r.organization_id = v_org_id
      AND r.name = 'APPROVER';

    SELECT r.id
    INTO v_finance_role
    FROM roles r
    WHERE r.organization_id = v_org_id
      AND r.name = 'FINANCE';


    -- =========================================================
    -- DEMO USERS
    -- =========================================================

    INSERT INTO users (
        organization_id,
        role_id,
        name,
        email,
        password_hash,
        active
    )
    VALUES
        (
            v_org_id,
            v_admin_role,
            'Demo Admin',
            'admin@example.com',
            '$2a$12$e/8tasyyfO/ako2DJc8Aeur9I78AW3.YYJkzSFtEfQ7wkB3SxLBA.',
            TRUE
        ),
        (
            v_org_id,
            v_submitter_role,
            'Demo Submitter',
            'submitter@example.com',
            '$2a$12$GOeqr2R96CinTTkz/SR4v.w27bWGkidA9m27nURUtpOQdYaIYl4zy',
            TRUE
        ),
        (
            v_org_id,
            v_approver_role,
            'Demo Approver',
            'approver@example.com',
            '$2a$12$RZBYsEppWSKSZ55MnkywV.lkHhA0qloRueGJgZ8RbkF1X4G.5qJpm',
            TRUE
        ),
        (
            v_org_id,
            v_finance_role,
            'Demo Finance',
            'finance@example.com',
            '$2a$12$mbBAEPXAvhRAmGFaN0ywKOg4rD1hrt1F9VsqDNMAynmOX7ZiK944u',
            TRUE
        )
    ON CONFLICT (organization_id, email)
    DO UPDATE SET
        name = EXCLUDED.name,
        role_id = EXCLUDED.role_id,
        password_hash = EXCLUDED.password_hash,
        active = TRUE;


    -- Get verifier users

    SELECT u.id
    INTO v_approver_user
    FROM users u
    WHERE u.organization_id = v_org_id
      AND u.email = 'approver@example.com';

    SELECT u.id
    INTO v_finance_user
    FROM users u
    WHERE u.organization_id = v_org_id
      AND u.email = 'finance@example.com';


    -- =========================================================
    -- DOCUMENT TYPE
    -- =========================================================

    INSERT INTO document_types (
        organization_id,
        name,
        description,
        active
    )
    VALUES (
        v_org_id,
        'Purchase Request',
        'Internal purchase approval',
        TRUE
    )
    ON CONFLICT (organization_id, name)
    DO UPDATE SET
        description = EXCLUDED.description,
        active = TRUE;


    -- Get document type ID

    SELECT d.id
    INTO v_doc_type
    FROM document_types d
    WHERE d.organization_id = v_org_id
      AND d.name = 'Purchase Request';


    -- =========================================================
    -- WORKFLOW
    -- =========================================================

    INSERT INTO workflows (
        organization_id,
        document_type_id,
        name,
        active
    )
    VALUES (
        v_org_id,
        v_doc_type,
        'Purchase Request Verification',
        TRUE
    )
    ON CONFLICT (
        organization_id,
        document_type_id,
        name
    )
    DO UPDATE SET
        active = TRUE;


    -- Get workflow ID

    SELECT w.id
    INTO v_workflow_id
    FROM workflows w
    WHERE w.organization_id = v_org_id
      AND w.document_type_id = v_doc_type
      AND w.name = 'Purchase Request Verification';


    -- =========================================================
    -- WORKFLOW STAGES
    -- ADMIN IS NOT A VERIFIER
    -- =========================================================

    INSERT INTO workflow_stages (
        workflow_id,
        stage_order,
        stage_name,
        user_id
    )
    VALUES
        (
            v_workflow_id,
            1,
            'Initial Verification',
            v_approver_user
        ),
        (
            v_workflow_id,
            2,
            'Finance Verification',
            v_finance_user
        )
    ON CONFLICT (
        workflow_id,
        stage_order
    )
    DO UPDATE SET
        stage_name = EXCLUDED.stage_name,
        user_id = EXCLUDED.user_id;

END
$$;