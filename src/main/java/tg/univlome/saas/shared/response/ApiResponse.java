package tg.univlome.saas.shared.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse générique pour toutes les API de l'application.
 *
 * @param <T> Le type des données renvoyées.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Crée une réponse de succès.
     *
     * @param data Les données à retourner.
     * @param message Le message de succès.
     * @param <T> Le type des données.
     * @return La réponse générique.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Crée une réponse d'erreur.
     *
     * @param message Le message d'erreur.
     * @param errors Les détails des erreurs.
     * @param <T> Le type des données.
     * @return La réponse générique.
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
