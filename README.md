# Golden Record Curator

Final project for the Epicode Java course. A small Java SE command line
application built around one real world scenario: the curation of the
**Voyager Golden Record** of 1977, the phonograph disc bolted to Voyager 1 and
Voyager 2 with 55 spoken greetings, 21 sounds of Earth, 27 musical selections
and 115 images encoded as audio.

We picked this domain because it maps onto the required patterns without
having to force any of them, and because the arithmetic is honest: the groove
holds about 110 minutes and the manifest we assembled from public sources runs
slightly over. That mismatch is what gives the `curate` command something to
do.

---

## 1. What the application does

The program answers three questions about a proposed manifest:

| Question | What it computes |
|---|---|
| Does it fit? | The recursive sum of every duration against the disc capacity |
| Is it representative? | How many languages and language families are covered |
| Is it engravable? | Whether the manifest parses and every content respects its type |

### Commands (quick reference)

```
load [file]      load a manifest (default: voyager-1977.csv)
tree             print the whole content tree
budget           show how full each side is
play [lang]      play a greeting by ISO 639-3 code (default: ita)
validate         run the three engraving rules
stats            count contents by type
curate [policy]  propose removals: coverage | count
help             list the commands
exit             quit
```

The dispatcher is in `CuratorConsole.dispatch()`; each command has its own
`do*` method in the same class. Every command runs inside `SafeExecutor`, so
an exception thrown by any of them turns into a shielded error and does not
break the loop.

### Commands (detailed)

#### `load [file]`

Loads a manifest from `data/<file>` (default: `voyager-1977.csv`) and builds
the in-memory `Disc`. This is the only command that touches the disk: every
other command works on the tree already loaded, so `load` must be the first
thing typed in a session.

The path is passed through `PathSanitizer`, which resolves, normalises and
verifies that the file still lives under the `data/` folder. Anything that
tries to escape (`../../etc/passwd`, absolute paths, symlinked oddities we
thought of) is refused with a shielded error and no crash. We use this in
the demo as our small proof that path traversal is handled.

Exercises: **Factory** (`ContentFactory` turns each CSV row into the right
class), **Java I/O** (`Files.newBufferedReader` with try-with-resources and
explicit UTF-8), **Exception Shielding** (bad file names come back as
`Result.failure`, not stack traces).

```
grc> load
  [OK] Manifest loaded - 218 contents, 55 languages
```

#### `tree`

Prints the whole Composite as an indented tree. Sections are marked with `+`
and show their aggregated duration and item count; leaves are marked with
`-`. `Greeting` and `MusicTrack` override the default rendering to add the
language code / composer.

The rendering itself is done by `Content.render(String)`, which returns a
`String` rather than writing to `System.out`. `CuratorConsole` is the only
place that hands that string to `ConsoleWriter`. This is what lets us keep
`System.out.print*` confined to a single file.

Exercises: **Composite** (recursive render with no `instanceof`), and the
architectural decision to keep I/O out of the domain.

```
grc> tree
  + The Sounds of Earth  [115:03, 218 items]
    + Side 1  [54:35, 108 items]
      + Greetings  [27:00, 55 items]
        + Ancient Tongues  [0:27, 6 items]
          - Sumerian [sux] (0:03)  "..."
          ...
```

#### `budget`

Shows how full each side is against its capacity, then the whole disc. This
is the command that answers the first of the three project questions:
*does it fit?*

Under the hood this calls `Section.getPlaybackTime()` and
`Disc.isOverBudget()`, both of which are recursive sums over the Composite.
No traversal code lives in `CuratorConsole` for this — that is the whole
point of the pattern.

Exercises: **Composite** (recursive aggregation), **Collections** (the sides
come from `Disc.getSides()`).

```
grc> budget
  Side 1      54:35 / 55:00  [OK]
  Side 2      60:28 / 55:00  [OVER]
  DISC       115:03 / 110:00  [OVER by 5:03]
```

#### `play [lang]`

Finds the first `Greeting` whose ISO 639-3 language code matches the
argument (default: `ita`) and prints it: language, era, spoken text.

Traversal uses `for (Content c : disc)` — which works because `Disc`
implements `Iterable<Content>` and returns a `PlaybackIterator`. There is
no recursion written in this method and no `instanceof` on containers, only
one on `Greeting` to read its extra fields.

If the code is not found, the method throws `ContentNotFoundException`,
which the shielding turns into a friendly line with a trace id.

Exercises: **Iterator** (`PlaybackIterator`, engraving order), **Exception
Shielding** (`ContentNotFoundException` is a domain exception with two
messages).

```
grc> play ita
  [0:03] GREETING - Italian - LIVING
         "Tanti auguri e saluti."
```

#### `validate`

Runs the three engraving rules and prints a verdict. Each rule is a private
method inside `DiscValidator`:

- **Format** — every content has a duration its type accepts (an image
  cannot last three minutes, a music track cannot last two seconds).
- **Capacity** — no side exceeds its budget, and the total does not exceed
  the disc capacity.
- **Coverage** — the number of distinct languages is above the configured
  threshold (`validation.coverage.min.languages` in `disc.properties`).

Each finding is a `ValidationIssue` labelled `[OK]` (rule satisfied) or
`[FAIL]` (rule broken). The final line counts the blocking failures.

Exercises: **Iterator** (`EngravableContents`, leaves-only traversal),
**Composite** (`disc.getLanguages()` aggregates recursively), **Collections**
(the issues are collected in an `ArrayList`).

```
grc> validate
  [OK]   Format - all 218 contents are encodable
  [FAIL] Capacity - Side 2 exceeds its budget by 5:28
  [FAIL] Capacity - the disc exceeds its total capacity by 5:03
  [OK]   Coverage - 55 languages represented
  Result: NOT ENGRAVABLE - 2 blocking issue(s)
```

#### `stats`

Counts leaves grouped by `ContentType`, plus the total number of distinct
languages. Uses `EngravableContents` (the leaves-only iterator) so sections
are skipped without any `instanceof`, and a `HashMap<ContentType, Integer>`
as the counter.

Exercises: **Collections** (`HashMap` for grouping), **Iterator**
(`EngravableContents`), **Generics** (parameterised map).

```
grc> stats
  GREETING      55
  EARTH_SOUND   21
  MUSIC         27
  IMAGE        115
  LANGUAGES     55
```

#### `curate [policy]`

Proposes what to remove from each over-budget side, using one of two
strategies picked at runtime from the argument:

- `coverage` (default) — `MaximizeLanguageCoverage`: prefers to remove items
  whose language is already covered by another item on the disc, so we
  free seconds without silencing a culture.
- `count` — `ShortestFirstPolicy`: greedy on duration, removes the shortest
  items first. Simple, sometimes brutal.

The policy is chosen with an `if` on the argument and passed to the code
that iterates over the sides. The dispatching code does not change when a
new policy is added — that is exactly why we used a Strategy.

Neither policy actually modifies the disc: they return a `List<Content>` of
*proposals*. Applying them is intentionally out of scope (see the
"limitations" section).

Exercises: **Strategy** (`CurationPolicy` + two implementations),
**Composite** (`side.getPlaybackTime()` on the recursive sum).

```
grc> curate coverage
  Policy: Maximize Language Coverage
  Removes items whose language is already spoken on the disc.

  Side 2 must free 5:28:
    - German (redundant with English)
    - Portuguese (redundant with Spanish)
    ...
  Proposed removals: 8, freeing 6:12
```

#### `help`

Prints the compact command list. Nothing else — no I/O, no domain access.

```
grc> help
  load [file]      load a manifest (default voyager-1977.csv)
  tree             print the whole content tree
  ...
```

#### `exit` (alias: `quit`)

Ends the session. The main loop closes the `Scanner` and prints a short
goodbye. `quit` is accepted as an alias because half of us always type one,
half always type the other.

### About the data

`data/voyager-1977.csv` contains 250 rows (32 sections, 218 real contents).
The 27 music durations are the ones NASA published, and their sum reproduces
the commonly cited "about 90 minutes of music", which is our main sanity
check. Greeting and Earth-sound durations were never published, so those are
**reconstructed** from typical spoken/sample lengths; image durations are
**derived** from a frame time we calibrated against the documented capacity.
We only claim the music figures as historical; everything else is our best
approximation and could be off.

---

## 2. Setup and execution

Requirements: **JDK 11 or later**. No runtime dependency (only JUnit and
Mockito, both scoped to `test`).

```bash
mvn clean package
java -cp target/classes it.aliounediagne.epicode.goldenrecord.App
```

Or without Maven, if the machine has no Maven installed:

```bash
javac -encoding UTF-8 -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes it.aliounediagne.epicode.goldenrecord.App
```

Launch from the project root: the program looks for `data/voyager-1977.csv`
and for `disc.properties` in the working directory when they are not on the
classpath.

A short sample session:

```
grc> load
  [OK] Manifest loaded - 218 contents, 55 languages

grc> budget
  Side 1      54:35 / 55:00  [OK]
  Side 2      60:28 / 55:00  [OVER]
  DISC       115:03 / 110:00  [OVER by 5:03]

grc> play ita
  [0:03] GREETING - Italian - LIVING
         "Tanti auguri e saluti."

grc> validate
  [OK]   Format - all 218 contents are encodable
  [FAIL] Capacity - Side 2 exceeds its budget by 5:28
  [FAIL] Capacity - the disc exceeds its total capacity by 5:03
  [OK]   Coverage - 55 languages represented
  Result: NOT ENGRAVABLE - 2 blocking issue(s)
```

---

## 3. Class diagram (UML, simplified)

```
                          <<interface>>
                             Content
       getId() getTitle() getType() getPlaybackTime()
       getItemCount() getLanguages() isContainer() render()
                                |
            +-------------------+--------------------+
            |                                        |
      AbstractContent                            Section
      (abstract, Leaf base)                      (Composite)
      - id, title, duration                      - children : List<Content>
      - validates in constructor                 - parent  (cycle guard)
            |                                        |
   +--------+--------+---------+                   Disc
   |        |        |         |                   (root, adds capacity,
Greeting MusicTrack EarthSound EncodedImage         implements Iterable)
            ^
            |
      MusicTrack.Builder   <-- optional: Builder


   <<interface>>                    <<interface>>
     Iterator<Content>              CurationPolicy      <-- optional: Strategy
          ^                                ^
   +------+-------+              +---------+---------+
PlaybackIterator  |    MaximizeLanguageCoverage  ShortestFirstPolicy
        EngravableIterator
```

## 4. Architectural diagram (data flow)

```
   USER
    |  types a command
    v
 CuratorConsole ------> InputSanitizer      whitelist, length, control chars
    |
    |  wraps EVERY command in
    v
 SafeExecutor  <----- intended single catch point of the application
    |                 success -> Result<T>          failure -> Result<T>
    |                                                 |            |
    |                                          userMessage    diagnostic
    |                                          + traceId      + stack trace
    |                                                 |            |
    |                                          ConsoleWriter   curator.log
    v
 DiscLoader ------> ManifestProvider ------> CsvManifestProvider
    |                (interface)                    |
    |                                          PathSanitizer   traversal guard
    |
    +------> ContentFactory ------> Greeting | MusicTrack | EarthSound
    |                               EncodedImage | Section
    v
   Disc  (the Composite tree, in memory)
    |
    +---> PlaybackIterator / EngravableIterator      traversal
    +---> DiscValidator                              format, capacity, coverage
    +---> CurationPolicy                             what to sacrifice
```

---

## 5. Technologies and patterns, with our reasoning

For every required item we tried to pick an implementation that felt natural
for the domain rather than adding it just to tick a box. Where we chose one
option over another we tried to write down why, so that we can defend the
decision (or admit it was wrong) at the exam.

### Required design patterns

**Composite** — `Content`, `Section`, `Disc`, four leaf classes.
The manifest is already a nested tree in the historical sources, so the
pattern fits without forcing anything. Our reasoning was that the total
duration must cross the hierarchy without knowing how deep it is or which
kinds of leaf it will meet, and Composite gives us exactly that. The main
alternative we considered was a flat `List` with `instanceof` in the caller;
we rejected it because the aggregation would have lived outside the model.

**Factory** — `ContentFactory`.
When the CSV row is read, the class to instantiate depends on the `type`
column, a runtime value. We concentrated the branching in `ContentFactory`
so the I/O module does not need to know about the four concrete leaf
classes. Adding a fifth leaf, in theory, requires one enum constant, one
class and one branch in the factory; no existing method should need to be
reopened. In practice we have not tried it, so we cannot swear that no
hidden coupling would surface.

**Iterator** — `PlaybackIterator`, `EngravableIterator`.
A `for-each` over `children` visits one level only; the tree we care about
is six deep. We put the traversal logic in two dedicated Iterators rather
than in `Section` so that the container keeps a single responsibility.
There are two of them because the two use cases have different orders
(engraving order vs leaves-only). Both use an explicit stack: a recursive
method cannot suspend between two `next()` calls, and an iterator is
pull-based.

**Exception Shielding** — `CuratorException`, `SafeExecutor`,
`SafeConsoleFormatter`, `ConsoleWriter`.
Every domain exception carries two messages: `getDiagnostic()` for the log
file and `getUserMessage()` for the terminal. `SafeExecutor` is meant to be
the single catch point in the application: every user command runs inside
it, and we correlate the two channels with a short trace id. This is the
place where we most fear having missed something — one code path we forgot
to wrap would defeat the whole shielding. We tried to help ourselves with a
targeted test and a couple of grep verifications (see the last block of
this section), but we cannot rule out that a specific input still slips
through.

### Optional patterns we added

Only two, both because the domain asked for them. We did not want to add
more just to raise the score, because a pattern is a cost paid in indirection
and it looks worse to have one that does not earn its place.

**Strategy** — `CurationPolicy`, `MaximizeLanguageCoverage`,
`ShortestFirstPolicy`. When the budget is exceeded, deciding what to cut is
an editorial question, and it admits several defensible answers. Two
independent axes (structure vs criterion) gave us two hierarchies.

**Builder** — `MusicTrack.Builder`. Seven fields, three of them optional; a
seven parameter constructor felt unreadable and easy to break by swapping
two String arguments, and public setters would have destroyed immutability.
Factory and Builder compose without overlap: the factory decides *which
class*, the builder decides *how to assemble it*.

We considered Chain of Responsibility for the validator and Observer for
budget warnings. In both cases the current straight code seemed clearer for
the size of the problem; if the requirements grew we would probably add them.

### Core technologies

| Technology | Where we used it, and why we picked it |
|---|---|
| **Collections** | `ArrayList` for children because the engraving order is part of the data and reading is constant while writing is rare. `HashMap` for the id index (O(1) lookup). `TreeSet` for languages, because we need uniqueness and a deterministic order so reports are reproducible. |
| **Generics** | `Result<T>` carries the outcome of any operation type-safely, so we do not need casts at the call sites. `CuratorAction<T>` is our own functional interface: `SafeExecutor` can run any block of work with it. |
| **Java I/O** | `Files.newBufferedReader` with try-with-resources for the CSV, `getResourceAsStream` for `disc.properties`, and explicit `StandardCharsets.UTF_8` because the manifest contains Cyrillic, Arabic, Devanagari and Chinese, and on Windows the platform default would corrupt them. |
| **Logging** | `java.util.logging` with two handlers. The file handler keeps everything, stack traces included. The console handler is installed but switched off, because our log messages are diagnostic and we do not want them on the user's terminal. |
| **JUnit** | Six test classes, 24 tests, covering the recursive aggregations, the cycle guard, the constructor validation, both iteration orders, path traversal and the shielding. It is not exhaustive: several classes have no dedicated test yet. |
| **Mockito** | `DiscLoaderTest` replaces the `ManifestProvider` with a mock so the assembler is tested without touching the disk. |

### Secure programming — what we tried to do

| Requirement | What we did about it |
|---|---|
| Input sanitization | `InputSanitizer` (whitelist of characters, max length, control characters stripped) and `PathSanitizer` (resolve + **normalize** + `startsWith` against the base folder, which is the defence against `../` traversal). |
| No hardcoded values | Capacities, thresholds and folders live in `disc.properties`, read at startup with documented fallbacks in `App.java`. |
| Controlled exception propagation | Domain exceptions carry a separate user message; `SafeExecutor` is meant to be the single boundary; the command loop should not be interruptible by any exception thrown below it. |

Two verifications we ran on ourselves. The patterns match only real call
sites, so a comment that mentions the rule does not create a false positive.
If either produces something unexpected, we missed something:

```bash
grep -rEn '\.printStackTrace\('  src/main/java     # we expect: no results
grep -rEn 'System\.out\.print'   src/main/java     # we expect: only ConsoleWriter
```

We are aware these are the easy things to check. A determined input we did
not think of, a race we did not model (the application is single-threaded on
purpose), or a subtle bug in `PathSanitizer` on an OS we did not test could
still bite.

---

## 6. Known limitations and things we did not tackle

We are being deliberately generous in this list, because half the point of
the exam is to know what we know and, especially, what we do not.

- **Not every duration is historical.** The 27 music durations are documented
  and their sum reproduces the published total, which we take as a reasonable
  check. Greeting and Earth-sound durations were never published and are
  reconstructed; image durations depend on a single free parameter (the frame
  time) that we calibrated against the documented capacity. If NASA one day
  published exact per-item figures, ours would very likely be slightly off.
- **The image frame time is the one free parameter of the reconstruction.**
  It is calibrated so the manifest lands slightly over budget, which is what
  keeps the curation commands meaningful. A different frame time would shift
  everything.
- **Exception shielding is a hope, not a proof.** `SafeExecutor` wraps every
  command in the current command loop, and we tested the visible cases, but
  we did not prove that no code path escapes it. A future command added
  without going through `executor.execute(...)` would silently break the
  guarantee. We would like to enforce this with a small check but have not
  written it.
- **Single-threaded by design.** The leaves are immutable and therefore thread
  safe on their own, but `Section`'s child list is not. Making it concurrent
  is a whole redesign (copy-on-write list, cycle guard rework) that we did
  not attempt.
- **Curation policies are heuristics.** Choosing a subset of items that
  maximises value under a capacity constraint is a variant of the knapsack
  problem, which is NP-hard. Our two policies are simple greedy heuristics
  and do not claim optimality.
- **No persistence layer.** The tree lives in memory; the manifest CSV is the
  only durable representation. An `export` command that writes the curated
  manifest back to disk is the natural next step and it is missing.
- **The CSV parser is minimal.** `CsvReader` handles quoted fields and
  escaped quotes, which is what our manifest needs. It does not handle
  embedded newlines inside a field, and we did not test it against malformed
  CSVs beyond a couple of cases.
- **Test coverage is uneven.** We wrote tests where a wrong result would be
  silent (the recursive aggregations, the cycle guard, path traversal, the
  shielding). `DiscValidator`, `ContentFactory`, `CurationPolicy` and a few
  others do not have their own test class yet.
- **Only tested on Windows 11 with JDK 21.** The pom targets Java 11 and we
  see no reason it should not run elsewhere, but we did not try Linux or
  macOS, and we did not try older JDKs.

If something in the demo does not match what we describe here, it is a bug on
our side that we did not catch; we would rather hear about it than pretend it
is not there.

---

## 7. Running the tests

```bash
mvn -B clean test
```

Six test classes, 24 tests, all green on JDK 21 with the pom targeting Java 11.
The surefire reports land in `target/surefire-reports/`.

---

## 8. License and author

Released under the [MIT License](LICENSE).

Written by **Alioune Diagne** as the final Java project for the Epicode
course. The Voyager Golden Record content described in the manifest is
public-domain material published by NASA; the durations that were never
published are reconstructions of ours, as explained in section 6.
