package it.aliounediagne.epicode.goldenrecord.validation;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Disc;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.iterator.EngravableContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Applica regole indipendenti: formato, capacita e copertura linguistica.
 * Ogni regola produce issue leggibili dalla CLI.
 */
public class DiscValidator {

    private final int minimumLanguages;


    public DiscValidator(int minimumLanguages) {
        this.minimumLanguages = minimumLanguages;
    }

    public List<ValidationIssue> validate(Disc disc) {
        List<ValidationIssue> issues = new ArrayList<ValidationIssue>();
        checkFormat(disc, issues);
        checkCapacity(disc, issues);
        checkCoverage(disc, issues);
        return issues;
    }


    private void checkFormat(Disc disc, List<ValidationIssue> issues) {
        int rejected = 0;
        for (Content content : new EngravableContents(disc)) {
            if (!content.getType().accepts(content.getPlaybackTime())) {
                issues.add(new ValidationIssue("Format",
                        "'" + content.getTitle() + "' has a duration its type does not accept",
                        true));
                rejected++;
            }
        }
        if (rejected == 0) {
            issues.add(new ValidationIssue("Format",
                    "all " + disc.getItemCount() + " contents are encodable", false));
        }
    }

    private void checkCapacity(Disc disc, List<ValidationIssue> issues) {
        boolean allFit = true;

        for (Section side : disc.getSides()) {
            Integer capacity = side.getCapacitySeconds();
            if (capacity == null) {
                continue;
            }
            int used = side.getPlaybackTime();
            if (used > capacity) {
                issues.add(new ValidationIssue("Capacity",
                        side.getTitle() + " exceeds its budget by " + format(used - capacity),
                        true));
                allFit = false;
            }
        }
        if (disc.isOverBudget()) {
            issues.add(new ValidationIssue("Capacity",
                    "the disc exceeds its total capacity by "
                            + format(-disc.getRemainingSeconds()),
                    true));
            allFit = false;
        }
        if (allFit) {
            issues.add(new ValidationIssue("Capacity", "every side fits in the groove", false));
        }
    }

    private void checkCoverage(Disc disc, List<ValidationIssue> issues) {
        Set<String> languages = disc.getLanguages();

        if (languages.size() < minimumLanguages) {
            issues.add(new ValidationIssue("Coverage",
                    "only " + languages.size() + " languages, at least "
                            + minimumLanguages + " are required",
                    true));
        } else {
            issues.add(new ValidationIssue("Coverage",
                    languages.size() + " languages represented", false));
        }
    }

    private static String format(int seconds) {
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }
}
