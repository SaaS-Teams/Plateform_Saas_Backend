package tg.univlome.saas.marketing.analytique.repositories.projections;

public interface DailyStatsProjection {
    String getDate();
    long getOpens();
    long getClicks();
}
