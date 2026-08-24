package it.aliounediagne.epicode.goldenrecord.manifest.provider;

import it.aliounediagne.epicode.goldenrecord.manifest.ManifestRecord;

import java.util.List;

public interface ManifestProvider {

    List<ManifestRecord> loadRecords(String source);
}
