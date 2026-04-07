---
description: Shows code coverage report after running unit and/or integration tests. Generates JaCoCo HTML report and displays a summary.
argument-hint: [unit|integration|all]
---

# Code Coverage: $ARGUMENTS

Generate and display the JaCoCo code coverage report.

## How JaCoCo Works in This Project

- Both unit tests (`./mvnw test`) and integration tests (`jacoco:prepare-agent failsafe:integration-test failsafe:verify`) write coverage data to the **same** `target/jacoco.exec` file.
- The HTML report at `target/site/jacoco/` is auto-generated after unit tests by the parent POM, but it is **NOT** auto-regenerated after integration tests.
- You **must** run `./mvnw jacoco:report` after integration tests to regenerate the report with combined coverage.

## Instructions

1. Determine the scope from the argument (default: `all`):
   - `unit` — coverage from unit tests only
   - `integration` — coverage from integration tests only
   - `all` — run both unit and integration tests for combined coverage

2. Run the appropriate test commands if tests haven't been run yet in this session:
   - **Unit tests:** `./mvnw test`
   - **Integration tests:** `./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify`
   - **All:** run unit tests first, then integration tests

3. **Always** regenerate the JaCoCo report after running tests (especially after integration tests):
   ```bash
   ./mvnw jacoco:report
   ```
   This reads `target/jacoco.exec` (which contains combined unit + integration coverage data) and produces the HTML/CSV/XML reports.

4. Parse the CSV report and display a coverage summary table:
   ```bash
   cat target/site/jacoco/jacoco.csv
   ```

   The CSV has columns: GROUP, PACKAGE, CLASS, INSTRUCTION_MISSED, INSTRUCTION_COVERED, BRANCH_MISSED, BRANCH_COVERED, LINE_MISSED, LINE_COVERED, COMPLEXITY_MISSED, COMPLEXITY_COVERED, METHOD_MISSED, METHOD_COVERED.

   Calculate percentages as: `covered / (missed + covered) * 100`

   Format the output as a readable markdown table showing per-package coverage:

   | Package | Class% | Method% | Line% | Branch% |
   |---------|--------|---------|-------|---------|

   Also show the overall totals at the bottom.

5. Tell the developer they can open the full HTML report at:
   ```
   target/site/jacoco/index.html
   ```
   Suggest opening it in a browser: `open target/site/jacoco/index.html` (macOS) or `xdg-open target/site/jacoco/index.html` (Linux).

6. Highlight any packages with line coverage below 70% as areas that may need more tests.

7. After showing the summary, suggest to the developer:
   - "Would you like me to write additional unit or integration tests to improve coverage for the low-coverage packages?"
   - If the developer agrees, analyze the uncovered lines by opening the HTML report for the specific package (e.g., `target/site/jacoco/com.test.routes.consumer.rest/index.html`) or reading the source files, identify untested code paths (error handling, edge cases, conditional branches), and write targeted tests following the existing test patterns in the project.
   - After writing the new tests, re-run the test suite and regenerate the coverage report to verify improvement.