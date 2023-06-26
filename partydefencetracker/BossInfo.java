package net.runelite.client.plugins.partydefencetracker;

enum BossInfo {
    ABYSSAL_SIRE("Abyssal Sire", 250.0D),
    ALCHEMICAL_HYDRA("Alchemical Hydra", 100.0D),
    AKKHA("Akkha", 80.0D),
    AKKHAS_SHADOW("Akkha's Shadow", 30.0D),
    ARTIO("Artio", 150.0D),
    BA_BA("Ba-Ba", 20.0D),
    CALLISTO("Callisto", 225.0D),
    CALVARION("Calvar'ion", 225.0D),
    CERBERUS("Cerberus", 110.0D),
    CHAOS_ELEMENTAL("Chaos Elemental", 270.0D),
    COMMANDER_ZILYANA("Commander Zilyana", 300.0D),
    CORE("<col=00ffff>Core</col>", 0.0D),
    CORPOREAL_BEAST("Corporeal Beast", 310.0D),
    DAGANNOTH_PRIME("Dagannoth Prime", 255.0D),
    DAGANNOTH_REX("Dagannoth Rex", 255.0D),
    DAGANNOTH_SUPREME("Dagannoth Supreme", 128.0D),
    ELIDINIS_WARDEN("Elidinis' Warden", 30.0D),
    GENERAL_GRAARDOR("General Graardor", 250.0D),
    GIANT_MOLE("Giant Mole", 200.0D),
    GREAT_OLM("Great Olm (Left claw)", 175.0D),
    ICE_DEMON("Ice Demon", 160.0D),
    KALPHITE_QUEEN("Kalphite Queen", 300.0D),
    KEPHRI("Kephri", 20.0D),
    KING_BLACK_DRAGON("King Black Dragon", 240.0D),
    KREE_ARRA("Kree'arra", 260.0D),
    KRIL_TSUTSAROTH("K'ril Tsutsaroth", 270.0D),
    NEX("Nex", 260.0D),
    NYLOCAS_VASILIAS("Nylocas Vasilias", 50.0D),
    OBELISK("<col=00ffff>Obelisk</col>", 40.0D),
    PESTILENT_BLOAT("Pestilent Bloat", 100.0D),
    PHANTOM_MUSPAH("Phantom Muspah", 200.0D),
    SARACHNIS("Sarachnis", 150.0D),
    SCORPIA("Scorpia", 180.0D),
    SKELETAL_MYSTIC("Skeletal Mystic", 187.0D),
    SKOTIZO("Skotizo", 200.0D),
    SOTETSEG("Sotetseg", 200.0D),
    SPINDEL("Spindel", 225.0D),
    TEKTON("Tekton", 205.0D),
    TEKTON_ENRAGED("Tekton (enraged)", 205.0D),
    THE_MAIDEN_OF_SUGADINTI("The Maiden of Sugadinti", 200.0D),
    TUMEKENS_WARDEN("Tumeken's Warden", 30.0D),
    TZKAL_ZUK("TzKal-Zuk", 260.0D),
    TZTOK_JAD("TzTok-Jad", 480.0D),
    VASA("Vasa Nistirio", 175.0D),
    VENENATIS("Venenatis", 321.0D),
    VETION("Vet'ion", 395.0D),
    VORKATH("Vorkath", 214.0D),
    XARPUS("Xarpus", 250.0D),
    ZEBAK("Zebak", 20.0D),
    ZULRAH("Zulrah", 300.0D);

    private final String name;

    private final double baseDef;

    public String getName() {
        return this.name;
    }

    public double getBaseDef() {
        return this.baseDef;
    }

    BossInfo(String name, double baseDef) {
        this.name = name;
        this.baseDef = baseDef;
    }

    static BossInfo getBoss(String bossName) {
        for (BossInfo boss : values()) {
            if (boss.name.contains(bossName))
                return boss;
        }
        return null;
    }

    static double getBaseDefence(String bossName) {
        BossInfo boss = getBoss(bossName);
        if (boss != null)
            return boss.baseDef;
        return 0.0D;
    }
}
