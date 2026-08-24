package it.aliounediagne.epicode.goldenrecord.curation;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;
import it.aliounediagne.epicode.goldenrecord.iterator.EngravableContents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy di curatela: libera spazio cercando prima saluti ridondanti nella
 * stessa famiglia linguistica.
 */
public class MaximizeLanguageCoverage implements CurationPolicy {

    @Override
    public List<Content> selectForRemoval(Section overBudget, int excessSeconds) {

        List<Content> victims = new ArrayList<Content>();
        int freed = 0;

        Map<String, List<Greeting>> byFamily = new HashMap<String, List<Greeting>>();
        List<Content> others = new ArrayList<Content>();

        for (Content content : new EngravableContents(overBudget)) {
            if (content instanceof Greeting) {
                Greeting greeting = (Greeting) content;
                String family = greeting.getLanguageFamily();
                if (!byFamily.containsKey(family)) {
                    byFamily.put(family, new ArrayList<Greeting>());
                }
                byFamily.get(family).add(greeting);
            } else {
                others.add(content);
            }
        }

        for (Map.Entry<String, List<Greeting>> entry : byFamily.entrySet()) {
            List<Greeting> family = entry.getValue();

            for (int i = family.size() - 1; i > 0 && freed < excessSeconds; i--) {
                Greeting victim = family.get(i);
                victims.add(victim);
                freed += victim.getPlaybackTime();
            }
        }

        if (freed < excessSeconds) {
            List<Content> sorted = new ArrayList<Content>(others);
            sorted.sort(Comparator.comparingInt(Content::getPlaybackTime).reversed());

            for (Content candidate : sorted) {
                if (freed >= excessSeconds) {
                    break;
                }
                victims.add(candidate);
                freed += candidate.getPlaybackTime();
            }
        }
        return victims;
    }

    @Override
    public String getRationale() {
        return "Removes redundant greetings while preserving at least one "
                + "representative per language family: coverage over quantity.";
    }

    @Override
    public String getName() {
        return "coverage";
    }
}
