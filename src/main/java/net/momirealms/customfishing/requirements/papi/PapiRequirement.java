package net.momirealms.customfishing.requirements.papi;

import java.util.HashMap;

public interface PapiRequirement {
    boolean isMet(HashMap<String, String> papiMap);
}
