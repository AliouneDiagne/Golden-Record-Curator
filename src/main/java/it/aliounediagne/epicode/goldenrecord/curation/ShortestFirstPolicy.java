package it.aliounediagne.epicode.goldenrecord.curation;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.iterator.EngravableContents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy alternativa: rimuove prima i contenuti piu lunghi per salvare il
 * maggior numero possibile di elementi.
 */
public class ShortestFirstPolicy implements CurationPolicy {

    @Override
    public List<Content> selectForRemoval(Section overBudget, int excessSeconds) {

        List<Content> candidates = new ArrayList<Content>();
        for (Content content : new EngravableContents(overBudget)) {
            candidates.add(content);
        }

        List<Content> sorted = candidates.stream()
                .sorted(Comparator.comparingInt(Content::getPlaybackTime).reversed())
                .collect(java.util.stream.Collectors.toList());

        List<Content> victims = new ArrayList<Content>();
        int freed = 0;
        for (Content candidate : sorted) {
            if (freed >= excessSeconds) {
                break;
            }
            victims.add(candidate);
            freed += candidate.getPlaybackTime();
        }
        return victims;
    }

    @Override
    public String getRationale() {
        return "Removes the longest contents first, maximising how many "
                + "distinct items survive on the disc.";
    }

    @Override
    public String getName() {
        return "count";
    }
}
