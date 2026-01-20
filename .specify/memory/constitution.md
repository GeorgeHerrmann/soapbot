<!--
SYNC IMPACT REPORT - Constitution Update
=========================================
Version Change: [TEMPLATE] → 1.0.0
Type: MAJOR - Initial constitution ratification

Changes Summary:
- NEW: All core principles defined for Java Discord bot development
- NEW: Code-first development approach established
- NEW: Reusability and modularity requirements codified
- NEW: Git workflow and branch management rules defined
- NEW: Discord API integration standards set
- NEW: Technical stack requirements documented
- NEW: Development workflow and governance rules established

Templates Status:
✅ plan-template.md - Reviewed, already aligned with constitution principles
✅ spec-template.md - Reviewed, user story prioritization aligns with incremental development
✅ tasks-template.md - Reviewed, task organization supports modular development

Follow-up TODOs:
- None - All placeholders filled with project-specific values
- Constitution ready for active use in feature development
-->

# SoapBot Constitution

## Core Principles

### I. Code-First, Clean Architecture

Code MUST be the primary deliverable; documentation supports but never replaces working code. Every feature MUST utilize existing codebases where applicable rather than reinventing solutions. Code MUST be highly organized, properly commented, and follow established patterns within the SoapBot architecture. New implementations MUST integrate cleanly with existing systems (ClientManager → SoapClient → feature modules).

**Rationale**: SoapBot operates on minimal resources (1 vcore, 1GB RAM) and values efficiency. Clean code that reuses existing infrastructure ensures maintainability and performance within resource constraints.

### II. Modular Feature Design

Features MUST be designed as modular, guild-isolated components that integrate with the SoapClient architecture. Each feature MUST be independently testable and maintainable. Features MUST respect the guild isolation model - most functionality operates per-guild through the SoapClient, not globally. Dependencies MUST be clearly documented and minimized.

**Rationale**: SoapBot's architecture centers on guild isolation, where each Discord server has its own SoapClient managing guild-specific features. This ensures scalability and prevents cross-guild interference.

### III. Git Discipline (NON-NEGOTIABLE)

Every task completion MUST be committed to Git immediately upon completion. Branch naming MUST follow the pattern `[###-feature-name]` where ### is the feature number from specs/. All feature development MUST occur on dedicated feature branches, never directly on master. Commit messages MUST be clear and descriptive, referencing the task or feature being implemented. Master branch remains in production; feature branches hold ongoing development.

**Rationale**: SoapBot's master branch remains in production, requiring strict branch discipline to maintain stability. Immediate commits ensure work is tracked and recoverable, preventing loss of progress.

### IV. Discord API Integration Standards

All Discord interactions MUST use Discord4J wrapper consistently. Bot responses MUST be user-friendly, clear, and follow established command patterns. Features MUST respect Discord rate limits and API best practices. Command implementations MUST extend or implement ParseableCommand/Command interfaces. Error messages MUST be helpful to end users while logging technical details internally.

**Rationale**: Consistency in Discord API usage ensures reliable bot behavior and prevents rate limiting or API violations that could affect all guilds.

### V. Incremental Development & Testing

Features MUST be broken down into independently testable user stories, each deliverable as an MVP increment. Test scenarios MUST be defined before implementation begins (though TDD execution is optional unless specified). Each user story MUST have clear acceptance criteria that can be verified. Feature development MUST follow the priority order (P1 → P2 → P3) established in specifications.

**Rationale**: Incremental delivery allows early feedback and reduces risk. Clear acceptance criteria ensure features meet user needs before being considered complete.

### VI. Resource Efficiency

All code MUST consider the 1 vcore, 1GB RAM server constraint. Features MUST NOT introduce memory leaks or excessive resource consumption. Long-running operations MUST be designed for efficiency. Database queries and API calls MUST be optimized. Performance regressions MUST be identified and addressed before merging.

**Rationale**: SoapBot runs on limited hardware; inefficient code directly impacts stability and user experience across all guilds.

## Technical Standards

### Technology Stack

**MANDATORY**:
- Java 19+ for all application code
- Discord4J 3.2.4+ as the Discord wrapper
- Maven for dependency management and builds
- Git for version control

**APPROVED INTEGRATIONS**:
- OpenAI API for AI features (GPT integration)
- YouTube, Soundcloud for audio playback
- External APIs as needed (documented in feature specs)

**CONFIGURATION**:
- All sensitive credentials (API keys, tokens) MUST be externalized to configuration files (never committed)
- Environment-specific settings MUST be manageable without code changes

### Code Organization

**PACKAGE STRUCTURE**: All code MUST reside under `com.georgster.*` package hierarchy. Features MUST be organized into logical packages (e.g., `music`, `events`, `game`, `economy`). Utility and shared code MUST live in `util` or appropriate cross-cutting packages.

**COMMENTING**: All public classes and complex methods MUST have clear JavaDoc comments. Non-obvious logic MUST include inline comments explaining the "why" not just the "what".

**NAMING**: Class, method, and variable names MUST be descriptive and follow Java conventions (PascalCase for classes, camelCase for methods/variables).

## Development Workflow

### Feature Development Process

1. **Specification**: Feature MUST have a spec document in `specs/[###-feature-name]/` before development begins
2. **Branch Creation**: Create feature branch named `[###-feature-name]` from master
3. **Implementation**: Develop according to spec, committing after each task completion
4. **Testing**: Verify acceptance criteria and test scenarios defined in spec
5. **Integration**: Ensure feature integrates cleanly with existing SoapClient architecture
6. **Documentation**: Update README or relevant docs if user-facing changes
7. **Merge**: Only merge to master when feature is complete, tested, and production-ready

### Task Management

Tasks MUST be defined in `specs/[###-feature-name]/tasks.md` using the tasks template. Each task MUST have a clear deliverable and file path. Tasks MUST be marked complete only when committed to Git. Dependencies between tasks MUST be clearly indicated. Parallel tasks MUST be identified with [P] marker.

### Quality Gates

Before merging to master:
- ✅ All acceptance criteria met
- ✅ Code follows established patterns and architecture
- ✅ No memory leaks or resource violations
- ✅ Guild isolation maintained (if applicable)
- ✅ Error handling implemented and tested
- ✅ Documentation updated
- ✅ All tasks committed to Git

## Governance

This constitution supersedes all other development practices for SoapBot. All feature development MUST comply with these principles. When principles conflict with user requests, document the conflict and seek clarification before proceeding.

**Amendments**: Constitution changes require:
1. Clear rationale for the change
2. Version increment following semantic versioning (MAJOR.MINOR.PATCH)
3. Review of dependent templates and documentation
4. Migration plan if changes affect existing features

**Compliance**: Feature specifications and implementation plans MUST include a "Constitution Check" section verifying alignment with these principles. Non-compliance MUST be explicitly justified with rationale.

**Version Control**: Constitution version follows semantic versioning:
- **MAJOR**: Backward incompatible governance changes or principle removals
- **MINOR**: New principles added or material expansions
- **PATCH**: Clarifications, wording improvements, non-semantic changes

---

**Version**: 1.0.0 | **Ratified**: 2026-01-19 | **Last Amended**: 2026-01-19
