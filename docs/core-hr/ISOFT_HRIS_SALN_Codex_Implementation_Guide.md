# ISOFT HRIS – SALN Module Implementation Guide for Codex

## 1. Purpose

Implement a complete **Statement of Assets, Liabilities and Net Worth (SALN)** module in the existing ISOFT HRIS.

This module is **NOT part of PRIME-HRM**.

PRIME-HRM remains limited to the four core HR maturity areas:

1. Recruitment, Selection and Placement (RSP)
2. Performance Management System (PMS/SPMS)
3. Learning and Development (L&D)
4. Rewards and Recognition (R&R)

SALN must be implemented as a **regular HRIS statutory/compliance module**, with:

- Employee-facing SALN filing in the **Employee Portal**
- HR/RCC administration, monitoring, review, compliance checking, and reporting in the **Human Resource / HR Management side**

The implementation must follow the current CSC SALN framework and must preserve the legal responsibility of the **declarant/employee** over the contents of the SALN.

---

# 2. Core Design Principle

The system must treat SALN as:

> **Employee Declaration + Agency Review and Compliance**

Do **NOT** implement SALN as a normal request workflow such as:

- Pending
- Approved
- Rejected

The HR/RCC reviewer is not approving whether the employee's assets, liabilities, or declarations are acceptable.

Instead, the agency reviews the SALN for:

- Timeliness
- Completeness
- Proper form
- Compliance with filing requirements

Recommended workflow statuses:

```text
DRAFT
SUBMITTED
UNDER_REVIEW
FOR_CORRECTION
RESUBMITTED
COMPLIANT
LOCKED
VOIDED
```

Optional administrative status if needed:

```text
OVERDUE
NOT_FILED
```

---

# 3. High-Level Architecture

```text
ISOFT HRIS
│
├── Employee Portal
│   └── SALN
│       ├── Create / Edit Draft
│       ├── Assets
│       ├── Liabilities
│       ├── Business Interests
│       ├── Financial Connections
│       ├── Relatives in Government
│       ├── Spouse / Children Information
│       ├── Preview
│       ├── Submit
│       ├── Correction / Resubmission
│       ├── Print / Download
│       └── Filing History
│
└── Human Resource / HR Management
    └── SALN Administration
        ├── Dashboard
        ├── Employee SALNs
        ├── Review & Compliance
        ├── Filing Monitoring
        ├── Filing Periods
        ├── Historical / Paper SALN Encoding
        ├── Reports
        ├── Repository Submission Tracking
        └── Audit Trail
```

---

# 4. User Roles

## 4.1 Employee / Declarant

The employee owns the declaration.

Employee must be able to:

- Create SALN
- Save as draft
- Edit draft
- Add/update/remove SALN items while draft
- Preview SALN
- Submit SALN
- View submitted SALN
- View filing history
- Receive correction requests
- Edit SALN returned for correction
- Resubmit corrected SALN
- Print or download the SALN
- View filing type, filing year, reference date, due date, and current status

Employee must NOT be able to edit a SALN after it becomes finally locked.

---

## 4.2 HR / SALN Administrator

HR must be able to:

- View all employee SALN records
- Filter by office, area, business unit, employee, filing year, filing type, and status
- Monitor who has filed and who has not filed
- View filing deadlines
- View submitted SALN
- Start review
- Return a SALN for correction
- Enter review remarks
- Mark SALN compliant
- Record repository submission information
- Generate compliance reports
- Print/export SALN documents
- View full audit trail
- Encode historical paper SALNs with proper source tagging
- Create filing requirements for employees when necessary
- Void an invalid duplicate or erroneous administrative record with reason

HR must NOT freely edit an employee's filed declaration.

---

## 4.3 RCC / Review and Compliance Committee

If the system supports a separate RCC role or permission set, allow RCC users to:

- View submitted SALNs
- Review for completeness and proper form
- Return for correction
- Add review remarks
- Mark compliant
- Record review date
- Record reviewer
- Generate review/compliance reports

If the existing RBAC system is used, create permissions rather than hardcoding roles.

Suggested permissions:

```text
saln.canAccess
saln.canCreate
saln.canSubmit
saln.canReview
saln.canReturnForCorrection
saln.canMarkCompliant
saln.canEncodeHistorical
saln.canVoid
saln.canPrint
saln.canViewAudit
saln.canManageFilingPeriod
```

---

# 5. Filing Types

Support the following SALN filing types:

```text
ASSUMPTION
ANNUAL
SEPARATION
```

## 5.1 Assumption SALN

Used upon assumption to office.

Store:

- assumption date
- filing requirement date
- due date
- actual submission date

The system should derive the due date from the employee's appointment/assumption data where available.

---

## 5.2 Annual SALN

Annual SALN must support:

```text
SALN Year
As-Of Date
Due Date
Actual Filing Date
```

Example:

```text
SALN Year: 2025
As Of: December 31, 2025
Due Date: April 30, 2026
```

Do not hardcode year values.

---

## 5.3 Separation SALN

Used upon separation from service.

Store:

- separation date
- separation type if available
- due date
- filing date

The requirement should be capable of being automatically generated from the employee separation module.

---

# 6. Portal Workflow

Recommended state transition:

```text
CREATE
  ↓
DRAFT
  ↓
SUBMIT
  ↓
SUBMITTED
  ↓
UNDER_REVIEW
  ├── Complete → COMPLIANT
  │                ↓
  │              LOCKED
  │
  └── Problem → FOR_CORRECTION
                    ↓
                Employee edits
                    ↓
                RESUBMITTED
                    ↓
                UNDER_REVIEW
                    ↓
                 COMPLIANT
                    ↓
                   LOCKED
```

Do not use generic approval flow.

Do not reuse unrelated request approval logic unless the existing workflow framework can represent this compliance process correctly.

---

# 7. Editing Rules

## 7.1 Draft

Employee may fully edit draft records.

Allowed:

- add
- edit
- delete items
- save
- preview
- discard draft

---

## 7.2 Submitted / Under Review

Employee cannot edit while under review.

HR/RCC must not directly modify declared financial data.

Reviewer may:

- add review remarks
- identify missing/incomplete data
- return for correction

---

## 7.3 For Correction

When returned for correction:

- employee regains editing access
- system retains prior submitted version
- correction must create audit/version history
- employee resubmits the SALN

Never silently overwrite the previous filed version.

---

## 7.4 Compliant / Locked

Once finalized/locked:

- no employee edits
- no HR edits to declaration data
- no physical deletion

If a rare administrative problem exists, use VOID with:

- reason
- voided by
- voided date/time
- linked replacement record if applicable

---

# 8. Historical / Paper SALN Encoding

HR may encode old SALN records for migration or historical archive.

This must be treated differently from a SALN filed electronically.

Required metadata:

```text
sourceType:
- ONLINE
- PAPER
- LEGACY_MIGRATION
- ADMIN_ENCODED
```

For paper/historical records store:

- declarant employee
- original filing date
- filing year
- filing type
- as-of date
- encoded by user
- encoded date/time
- source document reference
- optional scanned attachment
- remarks

UI must clearly indicate:

```text
Historical / Paper SALN
Encoded by HR
```

Do not make it appear that HR was the declarant.

---

# 9. SALN Data Structure

Codex must inspect the current CSC 2025 SALN form before finalizing database fields.

At minimum, design for the following sections.

## 9.1 SALN Header

Suggested fields:

```text
salnId
employeeId
employeeNo
filingType
salnYear
asOfDate
dueDate
submissionDate
status
sourceType
versionNo
createdAt
createdBy
updatedAt
updatedBy
submittedAt
submittedBy
reviewedAt
reviewedBy
compliantAt
compliantBy
lockedAt
repositorySubmittedAt
repositoryReferenceNo
remarks
```

---

## 9.2 Declarant Information

Use existing employee profile where possible.

Examples:

- full name
- position
- agency/office
- office address
- spouse details
- children details

Do not duplicate master employee data unnecessarily.

Snapshot required declarant data into the SALN record if the form legally needs historical values as of filing date.

---

## 9.3 Real Properties

Support multiple rows.

Suggested fields:

```text
propertyId
salnId
description
kind
exactLocation
assessedValue
currentFairMarketValue
acquisitionYear
acquisitionMode
acquisitionCost
```

---

## 9.4 Personal Properties

Support multiple rows.

Suggested fields:

```text
personalPropertyId
salnId
description
acquisitionYear
acquisitionCost
```

---

## 9.5 Liabilities

Support multiple rows.

Suggested fields:

```text
liabilityId
salnId
nature
creditorName
outstandingBalance
```

---

## 9.6 Business Interests / Financial Connections

Support multiple rows.

Suggested fields:

```text
businessInterestId
salnId
entityName
businessAddress
natureOfBusinessInterest
dateOfAcquisition
```

Support an explicit:

```text
NO_BUSINESS_INTERESTS
```

selection if required by the prescribed form.

---

## 9.7 Relatives in Government

Support multiple rows according to current CSC form requirements.

Suggested fields:

```text
relativeId
salnId
name
relationship
position
agencyOffice
```

Support an explicit declaration of none where applicable.

---

## 9.8 Spouse and Children

Use a child collection rather than fixed columns.

Suggested:

```text
salnDependent
- dependentId
- salnId
- name
- relationship
- age
- additional required form fields
```

The database must support current CSC additional sheets.

---

# 10. Computations

System may calculate:

```text
Total Real Property
Total Personal Property
Total Assets
Total Liabilities
Net Worth
```

Formula:

```text
TOTAL_ASSETS = TOTAL_REAL_PROPERTY + TOTAL_PERSONAL_PROPERTY

NET_WORTH = TOTAL_ASSETS - TOTAL_LIABILITIES
```

Use BigDecimal for currency.

Never use float/double for monetary values.

Use appropriate scale and rounding.

Example:

```java
BigDecimal
```

Database:

```text
NUMERIC(19,2)
```

or equivalent.

---

# 11. Validation

Before submission, validate required information.

Examples:

- filing type required
- SALN year required where applicable
- as-of date required
- declarant information present
- property values valid
- no negative monetary values unless explicitly permitted
- duplicate annual filing prevention
- required spouse/dependent declaration completed
- business interest declaration answered
- relatives-in-government declaration answered
- required certifications/affirmations completed

Use frontend validation for usability.

Repeat all critical validation in backend.

Never rely only on frontend validation.

---

# 12. Duplicate Filing Rule

Prevent accidental duplicate active filings for the same:

```text
employeeId
filingType
salnYear
asOfDate
```

Allow duplicates only for:

- versions
- voided records
- explicitly permitted replacement/corrective records

Use database constraints where practical.

---

# 13. Versioning

Every employee resubmission after correction must be versioned.

Example:

```text
SALN ID: 123
Version 1
Submitted: 2026-04-20
Returned for Correction: 2026-04-22

SALN ID: 123
Version 2
Resubmitted: 2026-04-23
Compliant: 2026-04-24
```

Recommended approach:

Parent:

```text
saln
```

Version/snapshot:

```text
saln_version
```

or preserve immutable versions using a revision table.

Do not destroy the original submitted contents.

---

# 14. Audit Trail

All important actions must be audited.

Audit event examples:

```text
SALN_CREATED
SALN_UPDATED
SALN_SUBMITTED
SALN_REVIEW_STARTED
SALN_RETURNED_FOR_CORRECTION
SALN_RESUBMITTED
SALN_MARKED_COMPLIANT
SALN_LOCKED
SALN_PRINTED
SALN_REPOSITORY_SUBMITTED
SALN_VOIDED
HISTORICAL_SALN_ENCODED
```

Audit fields:

```text
auditId
salnId
employeeId
action
performedBy
performedAt
oldStatus
newStatus
remarks
ipAddress (optional)
versionNo
```

Financial values should not be dumped unnecessarily into ordinary application logs.

---

# 15. Correction Workflow

Reviewer must enter a reason when returning for correction.

Recommended structure:

```text
Correction Request

SALN ID
Version
Reviewer
Date
General Remarks
Correction Items
```

Optional correction item fields:

```text
section
field
message
resolved
```

Example:

```text
Section: Real Property
Field: Acquisition Cost
Message: Acquisition cost is missing.
```

Employee sees correction remarks in Portal.

Employee corrects the data and resubmits.

---

# 16. Compliance Dashboard

Create a dashboard capable of showing:

```text
Total Employees Required to File
Filed
Not Filed
Draft
Submitted
Under Review
For Correction
Compliant
Overdue
```

Recommended filters:

- filing year
- filing type
- area
- business unit
- office
- employee
- status

Possible table:

```text
Employee No
Employee Name
Office
Filing Type
SALN Year
Due Date
Date Submitted
Status
Reviewer
Date Compliant
```

---

# 17. Filing Requirement Engine

If practical, generate SALN filing requirements automatically.

Examples:

## Annual

Generate an annual SALN requirement for eligible employees.

## Assumption

Trigger from active appointment/assumption date.

## Separation

Trigger from employee separation record.

Recommended entity:

```text
SalnFilingRequirement
```

Fields:

```text
requirementId
employeeId
filingType
referenceDate
salnYear
dueDate
status
generatedBy
generatedAt
linkedSalnId
```

This separates:

```text
Requirement to File
```

from:

```text
Actual SALN Filing
```

This is strongly recommended because it improves monitoring of NOT FILED / OVERDUE employees.

---

# 18. Notifications

Use the existing HRIS notification system if available.

Possible notifications:

```text
SALN filing requirement created
SALN due soon
SALN overdue
SALN successfully submitted
SALN returned for correction
SALN correction resubmitted
SALN marked compliant
```

Avoid exposing financial details in notification text.

Example:

```text
Your 2026 Annual SALN has been returned for correction.
Please review the comments in the SALN module.
```

---

# 19. PDF / Jasper Report

SALN PDF output must conform to the **current CSC 2025 prescribed SALN form**.

Do not reuse an obsolete 2015 form without verification.

Before implementing JRXML:

1. Obtain current official CSC SALN form.
2. Compare fields with system model.
3. Support additional sheets where required.
4. Match official layout as closely as practical.
5. Ensure values remain readable when rows exceed the first page.

Recommended report resources:

```text
reports/saln/
    saln_main.jrxml
    saln_additional_declarant.jrxml
    saln_spouse_children.jrxml
```

or an equivalent subreport strategy.

Generate PDF server-side.

Suggested endpoint:

```text
GET /api/saln/{salnId}/pdf
```

Authorization required.

---

# 20. Security and Privacy

SALN contains highly sensitive financial information.

Do not expose the module broadly.

Apply strict RBAC.

Recommended controls:

- employee can only access own SALN
- authorized HR/RCC users only
- server-side authorization on every endpoint
- do not trust employeeId from frontend
- validate employee identity from JWT/session
- mask unnecessary financial information in list pages
- HTTPS in production
- secure attachment access
- audit access where appropriate
- avoid logging SALN payloads in normal logs

Never provide an API such as:

```text
GET /api/saln/all
```

without authorization and filtering.

---

# 21. Suggested REST APIs

Codex must adjust paths according to existing project conventions.

## Employee Portal

```text
GET    /api/saln/my
GET    /api/saln/my/{id}
POST   /api/saln/my
PUT    /api/saln/my/{id}
DELETE /api/saln/my/{id}/draft
POST   /api/saln/my/{id}/submit
POST   /api/saln/my/{id}/resubmit
GET    /api/saln/my/{id}/pdf
```

## HR / RCC

```text
GET    /api/saln/admin
GET    /api/saln/admin/{id}
POST   /api/saln/admin/{id}/start-review
POST   /api/saln/admin/{id}/return-for-correction
POST   /api/saln/admin/{id}/mark-compliant
POST   /api/saln/admin/{id}/lock
POST   /api/saln/admin/{id}/repository-submission
POST   /api/saln/admin/historical
POST   /api/saln/admin/{id}/void
GET    /api/saln/admin/{id}/audit
GET    /api/saln/admin/report/compliance
```

---

# 22. Backend Implementation Rules

Project environment:

- Java 17
- Spring Boot 3.x
- Spring Security 6
- JWT authentication
- PostgreSQL / SQL Server compatibility where applicable

Follow the existing ISOFT HRIS patterns.

Use:

```text
Controller
Service
Repository
Entity
DTO
Mapper
Validator
Security/RBAC
Report Service
```

Do not place business logic directly inside controllers.

Use transactions for:

- submit
- return for correction
- resubmit
- mark compliant
- lock
- void

Example:

```java
@Transactional
public SalnDto submitSaln(...)
```

---

# 23. Frontend Implementation Rules

Existing frontend stack:

- Next.js 15
- React 18
- TypeScript
- SCSS modules
- SweetAlert2
- strict ESLint
- do not use `any`

Follow existing HRIS page patterns.

Do not introduce Tailwind.

Recommended Portal pages:

```text
/portal/saln
/portal/saln/new
/portal/saln/[id]
/portal/saln/[id]/edit
/portal/saln/history
```

Recommended HR pages:

```text
/hr/saln
/hr/saln/review
/hr/saln/[id]
/hr/saln/compliance
/hr/saln/reports
```

Adapt routing to current project structure rather than forcing these exact paths.

---

# 24. Portal UI

Main SALN page should show:

```text
Statement of Assets, Liabilities and Net Worth

Current Filing Requirement

Filing Type:
Annual

SALN Year:
2026

As Of:
December 31, 2026

Due Date:
April 30, 2027

Status:
Draft
```

Sections may use tabs or stepper:

```text
1. Declarant Information
2. Real Properties
3. Personal Properties
4. Liabilities
5. Business Interests / Financial Connections
6. Relatives in Government
7. Spouse / Children
8. Review & Submit
```

Display totals in final review.

---

# 25. Submission Confirmation

Before final submission show a confirmation dialog.

Suggested acknowledgement:

```text
I certify that I have reviewed the information in this SALN and that the
information submitted is my declaration.
```

Require explicit confirmation before Submit.

Do not allow HR to execute this certification on behalf of an employee for an online SALN.

Historical paper encoding is a separate workflow.

---

# 26. HR Review UI

Reviewer should see:

```text
Employee Information
Filing Information
SALN Summary
Assets
Liabilities
Net Worth
Business Interests
Relatives
Attachments
Version History
Audit Trail
Review Remarks
```

Actions:

```text
Return for Correction
Mark Compliant
Print
View PDF
```

Do not include:

```text
Approve Declaration
Reject Declaration
```

---

# 27. Locking Rules

Lock the SALN when the agency's workflow determines it is final.

At minimum, once final/repository submission has occurred:

```text
editable = false
```

Do not delete.

Any post-lock administrative action must be explicit and audited.

---

# 28. Repository Submission Tracking

Add support for tracking when the agency forwards/submits SALNs to the appropriate repository agency.

Suggested fields:

```text
repositoryAgency
repositorySubmittedAt
repositorySubmittedBy
repositoryReferenceNo
repositoryRemarks
```

Once repository submission is finalized, prevent normal correction workflows unless permitted by applicable rules.

Do not automatically assume all agencies use the same repository destination.

Make repository agency configurable if needed.

---

# 29. Data Migration

If old SALN data exists:

1. inspect existing schema
2. do not destroy existing records
3. write migration scripts
4. tag migrated records
5. preserve filing dates
6. validate totals
7. preserve attachments where possible

Do not recreate the database from scratch.

---

# 30. Testing Requirements

## Unit Tests

Test:

- net worth calculation
- total asset calculation
- due date logic
- status transitions
- duplicate filing prevention
- correction workflow
- lock restrictions
- authorization rules

## Integration Tests

Test:

```text
Employee creates draft
Employee edits draft
Employee submits
HR reviews
HR returns for correction
Employee edits
Employee resubmits
HR marks compliant
Record locks
PDF generates
```

Also test unauthorized access.

Example:

```text
Employee A must not be able to retrieve Employee B's SALN.
```

---

# 31. Status Transition Validation

Backend must strictly validate transitions.

Allowed example:

```text
DRAFT → SUBMITTED

SUBMITTED → UNDER_REVIEW

UNDER_REVIEW → FOR_CORRECTION

FOR_CORRECTION → RESUBMITTED

RESUBMITTED → UNDER_REVIEW

UNDER_REVIEW → COMPLIANT

COMPLIANT → LOCKED
```

Invalid example:

```text
DRAFT → COMPLIANT
LOCKED → DRAFT
Employee → MARK_COMPLIANT
HR → EDIT_EMPLOYEE_FINANCIAL_DATA
```

Reject invalid transitions.

---

# 32. Do Not Implement These Behaviors

Codex must NOT:

- place SALN inside the PRIME-HRM module
- use generic APPROVED/REJECTED request logic
- let HR freely change an employee's submitted declaration
- permanently delete submitted SALNs
- overwrite prior submitted versions
- expose another employee's SALN through Portal
- hardcode monetary totals
- use float/double for financial amounts
- hardcode current filing year
- hardcode a single reviewer
- hardcode a single office
- use obsolete SALN fields without checking the current CSC form
- bypass existing HRIS RBAC
- store passwords/signing credentials in SALN records
- log complete SALN financial payloads

---

# 33. Implementation Sequence for Codex

Codex should work autonomously through the following sequence.

## Phase 1 – Inspect Existing Codebase

Inspect:

- HR backend structure
- Employee Portal structure
- JWT/security implementation
- RBAC implementation
- Employee entity
- Appointment entity
- Separation entity
- existing report/Jasper structure
- audit patterns
- notification system
- database conventions

Do not start coding blindly.

Document what existing patterns will be reused.

---

## Phase 2 – Design

Prepare:

- entity relationship design
- SALN status enum
- filing type enum
- source type enum
- RBAC permissions
- API contract
- UI route plan
- workflow diagram

Reuse existing architecture where practical.

---

## Phase 3 – Database

Implement:

- SALN main table
- SALN child tables
- correction/review tables
- audit table
- filing requirement table
- repository submission fields
- proper indexes and uniqueness constraints

Use migration scripts.

---

## Phase 4 – Backend

Implement:

- entities
- repositories
- DTOs
- service layer
- validators
- status transition logic
- employee APIs
- HR/RCC APIs
- authorization
- audit logging

---

## Phase 5 – Employee Portal

Implement:

- SALN list/history
- create/edit wizard or sections
- dynamic item rows
- calculations
- preview
- submit
- correction display
- resubmit
- PDF access

---

## Phase 6 – HR/RCC Module

Implement:

- dashboard
- employee SALN list
- filters
- review page
- return-for-correction flow
- mark-compliant flow
- historical encoding
- repository submission tracking
- audit view

---

## Phase 7 – Jasper / PDF

Build against current official CSC SALN form.

Validate page overflow/additional sheets.

---

## Phase 8 – Testing

Run:

- backend tests
- frontend lint
- frontend type checking
- production build
- authorization tests
- workflow tests
- PDF tests

---

# 34. Codex Working Rules

Codex must:

1. Inspect the existing implementation first.
2. Reuse existing project architecture.
3. Avoid unnecessary refactoring outside SALN.
4. Preserve current working modules.
5. Make small logical commits/changes.
6. Run tests after meaningful backend changes.
7. Run frontend lint/type checks after UI changes.
8. Do not use `any`.
9. Do not introduce Tailwind.
10. Keep SQL portable where the HRIS supports multiple DB providers.
11. Do not invent CSC business rules.
12. Flag any legal/compliance ambiguity before hardcoding a rule.
13. Prefer configuration for deadlines/repository settings that can vary.
14. Preserve full auditability.
15. Treat SALN financial data as sensitive.

---

# 35. Acceptance Criteria

The module is complete when:

- Employee can create a SALN draft.
- Employee can fill all required current SALN sections.
- Totals and net worth calculate correctly.
- Employee can submit.
- Submitted data becomes non-editable during review.
- HR/RCC can review but cannot silently modify declaration data.
- HR/RCC can return for correction.
- Employee can correct and resubmit.
- Previous submitted version remains traceable.
- HR/RCC can mark compliant.
- Final SALN can be locked.
- Submitted SALNs cannot be permanently deleted.
- Historical paper SALNs can be encoded with source metadata.
- Filing requirements can be monitored.
- Annual/assumption/separation filing types are supported.
- HR dashboard identifies filed/not-filed/overdue employees.
- PDF matches the current CSC SALN form.
- Portal access is restricted to the employee's own SALN.
- HR/RCC access is RBAC-controlled.
- All critical actions appear in audit history.
- Production build and tests pass.

---

# 36. Final Instruction to Codex

Implement SALN as a **statutory HRIS compliance module**, not as PRIME-HRM and not as a generic approval request.

The employee/declarant must retain responsibility over the declaration.

HR/RCC controls:

- monitoring
- completeness review
- correction requests
- compliance marking
- filing administration
- repository tracking
- reports
- historical records
- audit

HR/RCC must never silently alter the declarant's submitted financial declaration.

Before coding fields or Jasper layout, verify the current CSC 2025 SALN form and align the implementation with the official prescribed structure.
