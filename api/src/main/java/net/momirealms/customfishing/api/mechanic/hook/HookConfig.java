package net.momirealms.customfishing.api.mechanic.hook;

import java.util.List;

public interface HookConfig {

    String id();

    List<String> lore();

    static Builder builder() {
        return new HookConfigImpl.BuilderImpl();
    }

    interface Builder {

        Builder id(String id);

        Builder lore(List<String> lore);

        HookConfig build();
    }
}
