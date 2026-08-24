package it.aliounediagne.epicode.goldenrecord.curation;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;

import java.util.List;


public interface CurationPolicy {


    List<Content> selectForRemoval(Section overBudget, int excessSeconds);

    String getRationale();


    String getName();
}
