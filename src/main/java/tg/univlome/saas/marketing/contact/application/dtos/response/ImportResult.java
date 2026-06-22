package tg.univlome.saas.marketing.contact.application.dtos.response;


public record ImportResult(
        int total,
        int imported,
        int ignored,
        int errors
) {}
