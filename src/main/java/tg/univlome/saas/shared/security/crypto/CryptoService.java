package tg.univlome.saas.shared.security.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service de chiffrement symétrique AES-256 pour sécuriser les jetons d'accès sensibles
 * avant persistance en base de données.
 */
@Slf4j
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKeySpec;

    public CryptoService(@Value("${saas.security.crypto.secret-key:SaasMarketingSecretKeyForAES256Encryption2026!}") String secretKey) {
        this.secretKeySpec = generateSecretKey(secretKey);
    }

    /**
     * Chiffre une chaîne de texte en clair via AES-256 et retourne une représentation Base64.
     *
     * @param plainText le texte brut à chiffrer
     * @return la chaîne chiffrée encodée en Base64
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("[CRYPTO SERVICE] Erreur lors du chiffrement AES : {}", e.getMessage());
            throw new RuntimeException("Erreur lors du chiffrement de la donnée sensible : " + e.getMessage(), e);
        }
    }

    /**
     * Déchiffre une chaîne de texte encodée en Base64 vers son texte brut d'origine.
     *
     * @param cipherText le texte chiffré en Base64
     * @return le texte déchiffré en clair
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[CRYPTO SERVICE] Erreur lors du déchiffrement AES : {}", e.getMessage());
            throw new RuntimeException("Erreur lors du déchiffrement de la donnée sensible : " + e.getMessage(), e);
        }
    }

    private SecretKeySpec generateSecretKey(String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            log.error("[CRYPTO SERVICE] Erreur de génération de la clé AES : {}", e.getMessage());
            throw new IllegalStateException("Clé de chiffrement AES invalide", e);
        }
    }
}
