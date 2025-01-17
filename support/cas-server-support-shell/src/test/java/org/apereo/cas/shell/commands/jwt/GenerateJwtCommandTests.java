package org.apereo.cas.shell.commands.jwt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateJwtCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateJwtCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser"));
    }

    @Test
    public void verifyBadSize() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --signingSecretSize -1 "));
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionSecretSize -1 "));
    }

    @Test
    public void verifyBadAlg() {
        assertDoesNotThrow(() -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionAlgorithm dir --encryptionMethod A128KW "));
        assertDoesNotThrow(
            () -> runShellCommand(() -> () -> "generate-jwt --subject casuser --encryptionAlgorithm A128KW --encryptionMethod A128CBC_HS256"));

    }

}

