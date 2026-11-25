package deadsignal.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The safehouse catalogue: six stat upgrades plus two consumables. */
public final class Shop {
    private final List<ShopItem> items = new ArrayList<>();
    private final List<ShopItem> skills = new ArrayList<>();

    public Shop() {
        for (Stat s : Stat.values()) items.add(new StatUpgrade(s));
        items.add(new GrenadeBundle());
        items.add(new MedkitBundle());
        for (deadsignal.skill.SkillType t : deadsignal.skill.SkillType.values()) skills.add(new SkillPurchase(t));
    }

    public List<ShopItem> getItems() { return Collections.unmodifiableList(items); }
    public List<ShopItem> getSkills() { return Collections.unmodifiableList(skills); }
}
