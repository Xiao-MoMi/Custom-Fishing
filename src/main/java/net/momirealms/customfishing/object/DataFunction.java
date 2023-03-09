package net.momirealms.customfishing.object;

import java.util.HashMap;
import java.util.UUID;

public abstract class DataFunction extends Function {

    protected final HashMap<UUID, Integer> triedTimes;

    public DataFunction() {
        this.triedTimes = new HashMap<>();
    }

    protected boolean checkTriedTimes(UUID uuid) {
        Integer previous = triedTimes.get(uuid);
        if (previous == null) {
            triedTimes.put(uuid, 1);
            return false;
        }
        else if (previous > 2) {
            triedTimes.remove(uuid);
            return true;
        }
        else {
            triedTimes.put(uuid, previous + 1);
            return false;
        }
    }
}
