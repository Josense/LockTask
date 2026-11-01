## Architecture Guidelines
- **Three-Layer Architecture:** Divide the app into Presentation, Domain, and Data layers, ensuring the Domain layer remains completely independent and is only depended upon by other layers.
- **Notification-Centric Decoupling:** Treat the core Notification feature as an isolated module/component, exposing only its domain interfaces (placed in the Domain layer) to the rest of the app to prevent high coupling.
- **MVI Pattern:** Implement MVI (Model-View-Intent) in the Presentation layer to enforce a predictable, unidirectional data flow driven by immutable UI states and explicit user intents.
- **Koin DI:** Decouple all dependencies via Koin by binding Data implementations to Domain interfaces and using lifecycle-aware scopes like `viewModel` for state holders.

## Kotlin Style
- Use `variable.not()` instead of the unary `!` operator for Boolean negation.

## Git Commit & Workflow Etiquette
- **Branch Naming Convention:** All branches must follow the `type/module/feature_description` format, where `type` aligns with the commit prefix (e.g., `feat`, `fix`), `module` targets the functional or page area, and `feature_description` is a snake_case summary.
- **Git Worktree Workflow:** Always create worktrees in the parent directory of the current repository, keeping them parallel to the main repository directory (e.g., `git worktree add ../<branch-name> <branch-name>`).
- **Commit Message Formatting:** Every commit must start with a valid prefix followed by a single space (e.g., `feat: `, `fix: `, `refactor: `, `docs: `, `style: `, `test: `).
- Commit messages should describe the behavior that changed, not the implementation details that changed.
- When the reason for the change is not obvious from the title, include a commit body that explains why the change was required.
