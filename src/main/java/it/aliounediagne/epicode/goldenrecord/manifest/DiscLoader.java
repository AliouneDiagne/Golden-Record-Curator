package it.aliounediagne.epicode.goldenrecord.manifest;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Disc;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.exceptions.ManifestException;
import it.aliounediagne.epicode.goldenrecord.factory.ContentFactory;
import it.aliounediagne.epicode.goldenrecord.logging.CuratorLogger;
import it.aliounediagne.epicode.goldenrecord.manifest.provider.ManifestProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Trasforma righe piatte del manifest in un albero Composite.
 */
public class DiscLoader {

    private static final Logger LOG = CuratorLogger.get(DiscLoader.class);

    private final ManifestProvider provider;
    private final ContentFactory factory;

    // HashMap per trovare i parent in O(1) mentre colleghiamo le righe.
    private final Map<String, Content> index = new HashMap<String, Content>();


    public DiscLoader(ManifestProvider provider, ContentFactory factory) {
        this.provider = provider;
        this.factory = factory;
    }


    public Disc load(String source, int totalCapacity, int sideCapacity) {

        List<ManifestRecord> rows = provider.loadRecords(source);
        index.clear();

        Disc disc = null;

        for (ManifestRecord row : rows) {

            if (row.getParentId() == null || row.getParentId().trim().isEmpty()) {
                disc = new Disc(row.getId(), row.getTitle(), totalCapacity, sideCapacity);
                index.put(row.getId(), disc);
            } else {
                index.put(row.getId(), factory.createContent(row));
            }
        }

        if (disc == null) {
            throw new ManifestException("Manifest '" + source + "' has no root row");
        }

        for (ManifestRecord row : rows) {
            String parentId = row.getParentId();
            if (parentId == null || parentId.trim().isEmpty()) {
                continue;
            }

            Content parent = index.get(parentId);
            Content child = index.get(row.getId());

            if (parent == null) {
                throw new ManifestException("Row '" + row.getId()
                        + "' refers to unknown parent '" + parentId + "'");
            }
            if (!(parent instanceof Section)) {
                throw new ManifestException("Row '" + row.getId()
                        + "' has a parent that cannot contain anything");
            }
            ((Section) parent).addContent(child);
        }

        LOG.info("Manifest loaded: " + disc.getItemCount() + " contents, "
                + disc.getPlaybackTime() + "s total");
        return disc;
    }

    public Map<String, Content> getIndex() {
        return index;
    }
}
