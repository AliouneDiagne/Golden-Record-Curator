package it.aliounediagne.epicode.goldenrecord.cli;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.ContentType;
import it.aliounediagne.epicode.goldenrecord.content.Disc;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;
import it.aliounediagne.epicode.goldenrecord.curation.CurationPolicy;
import it.aliounediagne.epicode.goldenrecord.curation.MaximizeLanguageCoverage;
import it.aliounediagne.epicode.goldenrecord.curation.ShortestFirstPolicy;
import it.aliounediagne.epicode.goldenrecord.exceptions.ContentNotFoundException;
import it.aliounediagne.epicode.goldenrecord.exceptions.NoManifestLoadedException;
import it.aliounediagne.epicode.goldenrecord.exceptions.Result;
import it.aliounediagne.epicode.goldenrecord.exceptions.SafeExecutor;
import it.aliounediagne.epicode.goldenrecord.iterator.EngravableContents;
import it.aliounediagne.epicode.goldenrecord.manifest.DiscLoader;
import it.aliounediagne.epicode.goldenrecord.validation.DiscValidator;
import it.aliounediagne.epicode.goldenrecord.validation.ValidationIssue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Il command loop: legge una riga, la esegue, stampa il risultato, ricomincia.
 *
 * Il loop non puo essere rotto da un'eccezione: ogni comando gira dentro
 * SafeExecutor, che cattura tutto e restituisce un Result.
 *
 * Nessun collaboratore viene costruito qui dentro: arrivano tutti dal
 * costruttore (dependency injection), cosi il wiring resta concentrato in
 * App.main e la classe e testabile.
 */
public class CuratorConsole {

    private final DiscLoader loader;
    private final SafeExecutor executor;
    private final ConsoleWriter console;
    private final InputSanitizer sanitizer;
    private final int totalCapacity;
    private final int sideCapacity;
    private final int minimumLanguages;

    /** L'albero attualmente in memoria. Null fino al primo 'load'. */
    private Disc disc;

    public CuratorConsole(DiscLoader loader, SafeExecutor executor, ConsoleWriter console,
            InputSanitizer sanitizer, int totalCapacity, int sideCapacity,
            int minimumLanguages) {
        this.loader = loader;
        this.executor = executor;
        this.console = console;
        this.sanitizer = sanitizer;
        this.totalCapacity = totalCapacity;
        this.sideCapacity = sideCapacity;
        this.minimumLanguages = minimumLanguages;
    }

    /** Esegue la sessione interattiva finche l'utente non scrive 'exit'. */
    public void run() {
        console.banner();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            console.prompt("grc> ");
            if (!scanner.hasNextLine()) {
                break; // EOF (Ctrl+D / Ctrl+Z)
            }
            String rawLine = scanner.nextLine();

            // Ogni comando passa da qui: nessuna eccezione puo scappare.
            Result<Boolean> result = executor.execute("COMMAND",
                    () -> dispatch(rawLine));

            if (result.isSuccess()) {
                running = result.getValue();
            } else {
                console.error(result.getUserMessage(), result.getTraceId());
            }
        }
        console.println("");
        console.println("  Session closed. The disc keeps travelling.");
        scanner.close();
    }

    /**
     * Parsing + dispatch di UN comando.
     *
     * @param rawLine testo grezzo digitato dall'utente
     * @return false quando la sessione deve terminare
     */
    private boolean dispatch(String rawLine) {
        // Filtro di sicurezza sul testo grezzo (whitelist + control chars).
        String line = sanitizer.sanitizeCommand(rawLine);
        if (line.isEmpty()) {
            return true;
        }

        String[] parts = line.split("\\s+");
        String verb = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : null;

        if (verb.equals("exit") || verb.equals("quit")) {
            return false;
        } else if (verb.equals("help")) {
            printHelp();
        } else if (verb.equals("load")) {
            doLoad(argument);
        } else if (verb.equals("tree")) {
            doTree();
        } else if (verb.equals("budget")) {
            doBudget();
        } else if (verb.equals("play")) {
            doPlay(argument);
        } else if (verb.equals("validate")) {
            doValidate();
        } else if (verb.equals("stats")) {
            doStats();
        } else if (verb.equals("curate")) {
            doCurate(argument);
        } else {
            console.println("  Unknown command: '" + verb + "'. Type 'help'.");
        }
        return true;
    }

    // =======================================================================
    // COMANDI
    // =======================================================================

    /** load [file] - carica un manifest CSV. */
    private void doLoad(String fileName) {
        String safeName = sanitizer.sanitizeFileName(
                fileName == null ? "voyager-1977.csv" : fileName);

        disc = loader.load(safeName, totalCapacity, sideCapacity);

        console.println("  [OK] Manifest loaded - " + disc.getItemCount()
                + " contents, " + disc.getLanguages().size() + " languages");
    }

    /** tree - stampa l'intero Composite indentato: la ricorsione sta in render(). */
    private void doTree() {
        requireDisc("tree");
        console.println("");
        console.println(disc.render("  "));
        console.println("");
    }

    /** budget - mostra riempimento per lato e totale. */
    private void doBudget() {
        requireDisc("budget");
        console.println("");
        for (Section side : disc.getSides()) {
            Integer capacity = side.getCapacitySeconds();
            int used = side.getPlaybackTime(); // aggregazione ricorsiva
            String verdict = (capacity != null && used > capacity) ? "  [OVER]" : "  [OK]";
            console.println("  " + pad(side.getTitle(), 12)
                    + ConsoleWriter.formatTime(used) + " / "
                    + ConsoleWriter.formatTime(capacity == null ? 0 : capacity) + verdict);
        }
        console.println("  " + pad("DISC", 12)
                + ConsoleWriter.formatTime(disc.getPlaybackTime()) + " / "
                + ConsoleWriter.formatTime(disc.getTotalCapacitySeconds())
                + (disc.isOverBudget()
                        ? "  [OVER by " + ConsoleWriter.formatTime(-disc.getRemainingSeconds()) + "]"
                        : "  [OK]"));
        console.println("");
    }

    /**
     * play [lang] - trova il saluto per codice ISO 639-3. Il for-each su
     * 'disc' usa PlaybackIterator: la ricorsione la scrive l'iteratore.
     */
    private void doPlay(String languageCode) {
        requireDisc("play");
        String code = sanitizer.sanitizeId(languageCode == null ? "ita" : languageCode);

        for (Content content : disc) { // PlaybackIterator, ordine di incisione
            if (content instanceof Greeting) {
                Greeting greeting = (Greeting) content;
                if (greeting.getLanguageCode().equalsIgnoreCase(code)) {
                    console.println("");
                    console.println("  [" + ConsoleWriter.formatTime(greeting.getPlaybackTime())
                            + "] GREETING - " + greeting.getTitle()
                            + " - " + greeting.getEra());
                    console.println("         \"" + greeting.getSpokenText() + "\"");
                    console.println("");
                    return;
                }
            }
        }
        throw new ContentNotFoundException(code);
    }

    /** validate - esegue le tre regole di incidibilita e stampa il verdetto. */
    private void doValidate() {
        requireDisc("validate");

        List<ValidationIssue> issues = new DiscValidator(minimumLanguages).validate(disc);

        console.println("");
        int blocking = 0;
        for (ValidationIssue issue : issues) {
            console.println("  " + issue);
            if (issue.isBlocking()) {
                blocking++;
            }
        }
        console.println("  Result: " + (blocking == 0 ? "ENGRAVABLE" : "NOT ENGRAVABLE")
                + " - " + blocking + " blocking issue(s)");
        console.println("");
    }

    /** stats - conta le foglie per ContentType (EngravableContents salta le sezioni). */
    private void doStats() {
        requireDisc("stats");

        Map<ContentType, Integer> perType = new HashMap<ContentType, Integer>();
        for (Content content : new EngravableContents(disc)) {
            ContentType type = content.getType();
            perType.put(type, perType.getOrDefault(type, 0) + 1);
        }

        console.println("");
        for (ContentType type : ContentType.values()) {
            if (perType.containsKey(type)) {
                console.println("  " + pad(type.name(), 14) + perType.get(type));
            }
        }
        Set<String> languages = disc.getLanguages();
        console.println("  " + pad("LANGUAGES", 14) + languages.size());
        console.println("");
    }

    /**
     * curate [policy] - propone rimozioni per rientrare nel budget. Strategy:
     * la policy e scelta a runtime e il flusso del comando non cambia.
     */
    private void doCurate(String policyName) {
        requireDisc("curate");

        CurationPolicy policy = "count".equalsIgnoreCase(policyName)
                ? new ShortestFirstPolicy()
                : new MaximizeLanguageCoverage();

        console.println("");
        console.println("  Policy: " + policy.getName());
        console.println("  " + policy.getRationale());

        boolean anyProposal = false;
        for (Section side : disc.getSides()) {
            Integer capacity = side.getCapacitySeconds();
            if (capacity == null || side.getPlaybackTime() <= capacity) {
                continue; // questo lato sta gia dentro il budget
            }
            int excess = side.getPlaybackTime() - capacity;
            List<Content> victims = policy.selectForRemoval(side, excess);

            console.println("");
            console.println("  " + side.getTitle() + " must free "
                    + ConsoleWriter.formatTime(excess) + ":");

            List<String> names = new ArrayList<String>();
            int freed = 0;
            for (Content victim : victims) {
                names.add(victim.getTitle());
                freed += victim.getPlaybackTime();
            }
            for (String name : names) {
                console.println("    - " + name);
            }
            console.println("  Proposed removals: " + victims.size()
                    + ", freeing " + ConsoleWriter.formatTime(freed));
            anyProposal = true;
        }
        if (!anyProposal) {
            console.println("  Nothing to curate: every side fits.");
        }
        console.println("");
    }

    private void printHelp() {
        console.println("");
        console.println("  load [file]      load a manifest (default voyager-1977.csv)");
        console.println("  tree             print the whole content tree");
        console.println("  budget           show how full each side is");
        console.println("  play [lang]      play a greeting by ISO code (default ita)");
        console.println("  validate         run the validation chain");
        console.println("  stats            count contents by type");
        console.println("  curate [policy]  propose removals: coverage | count");
        console.println("  exit             quit");
        console.println("");
    }

    /**
     * Guardia usata da ogni comando che richiede un disco caricato.
     * @throws NoManifestLoadedException se ancora non e stato eseguito 'load'
     */
    private void requireDisc(String command) {
        if (disc == null) {
            throw new NoManifestLoadedException(command);
        }
    }

    /** Padding a destra fino a "width" caratteri. Utility di formatting. */
    private static String pad(String text, int width) {
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
