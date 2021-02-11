package net.utory.rankpoint.data;


public class MigrateManager {

    private DatabaseManager to;
    private DatabaseManager from;

    public MigrateManager(DatabaseManager to, DatabaseManager from) {
        this.to = to;
        this.from = from;
    }

    public void migrate() {
        from.loadAllPoints(to::savePoint);
        from.closeDatabase();
        to.closeDatabase();
    }
}
