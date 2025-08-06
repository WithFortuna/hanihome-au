package com.hanihome.hanihome_au_api.unit.domain.valueobject;

import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Nested
    @DisplayName("Email Creation")
    class EmailCreationTests {

        @Test
        @DisplayName("Should create email with valid format")
        void should_CreateEmail_When_ValidFormatProvided() {
            // Arrange
            String validEmail = "test@example.com";

            // Act
            Email email = new Email(validEmail);

            // Assert
            assertThat(email).isNotNull();
            assertThat(email.getValue()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("Should create email using of() factory method")
        void should_CreateEmail_Using_OfFactoryMethod() {
            // Arrange
            String validEmail = "user@domain.org";

            // Act
            Email email = Email.of(validEmail);

            // Assert
            assertThat(email.getValue()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void should_NormalizeEmail_ToLowercase() {
            // Arrange
            String mixedCaseEmail = "Test.User@EXAMPLE.COM";

            // Act
            Email email = new Email(mixedCaseEmail);

            // Assert
            assertThat(email.getValue()).isEqualTo("test.user@example.com");
        }

        @Test
        @DisplayName("Should trim whitespace from email")
        void should_TrimWhitespace_FromEmail() {
            // Arrange
            String emailWithWhitespace = "  test@example.com  ";

            // Act
            Email email = new Email(emailWithWhitespace);

            // Assert
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@example.com",
            "test.email@domain.org",
            "user123@test-domain.net",
            "user+tag@example.com",
            "user_name@example-domain.com",
            "123@domain.co.uk",
            "a@b.co",
            "very.long.email.address@very-long-domain-name.com"
        })
        @DisplayName("Should accept valid email formats")
        void should_AcceptValidEmailFormats(String validEmail) {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> new Email(validEmail));
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void should_ThrowException_When_EmailIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when email is empty or whitespace")
        void should_ThrowException_When_EmailIsEmptyOrWhitespace(String invalidEmail) {
            // Act & Assert
            assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-email",
            "@domain.com",
            "user@",
            "user.domain.com",
            "user@@domain.com",
            "user@domain",
            "user@.com",
            "user@domain.",
            ".user@domain.com",
            "user.@domain.com",
            "user..name@domain.com",
            "user@domain..com",
            "user name@domain.com",
            "user@domain com",
            "user@",
            "@",
            "user@domain.c"
        })
        @DisplayName("Should throw exception for invalid email formats")
        void should_ThrowException_ForInvalidEmailFormats(String invalidEmail) {
            // Act & Assert
            assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
        }
    }

    @Nested
    @DisplayName("Email Parsing")
    class EmailParsingTests {

        @Test
        @DisplayName("Should extract domain correctly")
        void should_ExtractDomain_Correctly() {
            // Arrange
            Email email = new Email("test.user@example.com");

            // Act
            String domain = email.getDomain();

            // Assert
            assertThat(domain).isEqualTo("example.com");
        }

        @Test
        @DisplayName("Should extract local part correctly")
        void should_ExtractLocalPart_Correctly() {
            // Arrange
            Email email = new Email("test.user@example.com");

            // Act
            String localPart = email.getLocalPart();

            // Assert
            assertThat(localPart).isEqualTo("test.user");
        }

        @Test
        @DisplayName("Should handle complex domain extraction")
        void should_HandleComplexDomain_Extraction() {
            // Arrange
            Email email = new Email("user@sub.domain.co.uk");

            // Act
            String domain = email.getDomain();

            // Assert
            assertThat(domain).isEqualTo("sub.domain.co.uk");
        }

        @Test
        @DisplayName("Should handle complex local part extraction")
        void should_HandleComplexLocalPart_Extraction() {
            // Arrange
            Email email = new Email("first.last+tag@domain.com");

            // Act
            String localPart = email.getLocalPart();

            // Assert
            assertThat(localPart).isEqualTo("first.last+tag");
        }

        @Test
        @DisplayName("Should handle single character local part")
        void should_HandleSingleCharacter_LocalPart() {
            // Arrange
            Email email = new Email("a@domain.com");

            // Act
            String localPart = email.getLocalPart();
            String domain = email.getDomain();

            // Assert
            assertThat(localPart).isEqualTo("a");
            assertThat(domain).isEqualTo("domain.com");
        }

        @Test
        @DisplayName("Should handle minimal valid domain")
        void should_HandleMinimalValid_Domain() {
            // Arrange
            Email email = new Email("user@a.co");

            // Act
            String localPart = email.getLocalPart();
            String domain = email.getDomain();

            // Assert
            assertThat(localPart).isEqualTo("user");
            assertThat(domain).isEqualTo("a.co");
        }
    }

    @Nested
    @DisplayName("Email Equality and Hash Code")
    class EmailEqualityAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when email values are the same")
        void should_BeEqual_When_EmailValuesAreSame() {
            // Arrange
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("test@example.com");

            // Act & Assert
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Should be equal when created with different cases")
        void should_BeEqual_When_CreatedWithDifferentCases() {
            // Arrange
            Email email1 = new Email("Test@Example.COM");
            Email email2 = new Email("test@example.com");

            // Act & Assert
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Should be equal when created with different whitespace")
        void should_BeEqual_When_CreatedWithDifferentWhitespace() {
            // Arrange
            Email email1 = new Email("  test@example.com  ");
            Email email2 = new Email("test@example.com");

            // Act & Assert
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when email values are different")
        void should_NotBeEqual_When_EmailValuesAreDifferent() {
            // Arrange
            Email email1 = new Email("test1@example.com");
            Email email2 = new Email("test2@example.com");

            // Act & Assert
            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("Should not be equal when domains are different")
        void should_NotBeEqual_When_DomainsAreDifferent() {
            // Arrange
            Email email1 = new Email("test@example1.com");
            Email email2 = new Email("test@example2.com");

            // Act & Assert
            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void should_NotBeEqual_ToNull() {
            // Arrange
            Email email = new Email("test@example.com");

            // Act & Assert
            assertThat(email).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to object of different class")
        void should_NotBeEqual_ToObjectOfDifferentClass() {
            // Arrange
            Email email = new Email("test@example.com");
            String notEmail = "test@example.com";

            // Act & Assert
            assertThat(email).isNotEqualTo(notEmail);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void should_BeEqual_ToItself() {
            // Arrange
            Email email = new Email("test@example.com");

            // Act & Assert
            assertThat(email).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("Email String Representation")
    class EmailStringRepresentationTests {

        @Test
        @DisplayName("Should return email value as toString")
        void should_ReturnEmailValue_AsToString() {
            // Arrange
            Email email = new Email("test@example.com");

            // Act
            String result = email.toString();

            // Assert
            assertThat(result).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return normalized email as toString")
        void should_ReturnNormalizedEmail_AsToString() {
            // Arrange
            Email email = new Email("  Test.User@EXAMPLE.COM  ");

            // Act
            String result = email.toString();

            // Assert
            assertThat(result).isEqualTo("test.user@example.com");
        }

        @Test
        @DisplayName("Should maintain string representation consistency")
        void should_MaintainStringRepresentation_Consistency() {
            // Arrange
            Email email = new Email("user@domain.org");

            // Act
            String toString1 = email.toString();
            String toString2 = email.toString();
            String getValue = email.getValue();

            // Assert
            assertThat(toString1).isEqualTo(toString2);
            assertThat(toString1).isEqualTo(getValue);
        }
    }

    @Nested
    @DisplayName("Email Immutability")
    class EmailImmutabilityTests {

        @Test
        @DisplayName("Should not allow modification after creation")
        void should_NotAllowModification_AfterCreation() {
            // Arrange
            String originalEmail = "test@example.com";
            Email email = new Email(originalEmail);

            // Assert - Value should remain constant (enforced by final modifier)
            assertThat(email.getValue()).isEqualTo(originalEmail);
            
            // Multiple calls should return the same value
            String value1 = email.getValue();
            String value2 = email.getValue();
            assertThat(value1).isSameAs(value2);
        }

        @Test
        @DisplayName("Should maintain consistent domain and local part extraction")
        void should_MaintainConsistent_DomainAndLocalPartExtraction() {
            // Arrange
            Email email = new Email("test.user@example.domain.com");

            // Act - Multiple calls
            String domain1 = email.getDomain();
            String domain2 = email.getDomain();
            String localPart1 = email.getLocalPart();
            String localPart2 = email.getLocalPart();

            // Assert - Should return consistent results
            assertThat(domain1).isEqualTo(domain2);
            assertThat(localPart1).isEqualTo(localPart2);
            assertThat(domain1).isEqualTo("example.domain.com");
            assertThat(localPart1).isEqualTo("test.user");
        }
    }

    @Nested
    @DisplayName("Email Business Logic")
    class EmailBusinessLogicTests {

        @Test
        @DisplayName("Should handle common email providers correctly")
        void should_HandleCommonEmailProviders_Correctly() {
            // Arrange & Act
            Email gmailEmail = new Email("user@gmail.com");
            Email outlookEmail = new Email("user@outlook.com");
            Email yahooEmail = new Email("user@yahoo.com.au");
            Email corporateEmail = new Email("employee@company.com.au");

            // Assert
            assertThat(gmailEmail.getDomain()).isEqualTo("gmail.com");
            assertThat(outlookEmail.getDomain()).isEqualTo("outlook.com");
            assertThat(yahooEmail.getDomain()).isEqualTo("yahoo.com.au");
            assertThat(corporateEmail.getDomain()).isEqualTo("company.com.au");
        }

        @Test
        @DisplayName("Should handle Australian domain extensions")
        void should_HandleAustralianDomainExtensions_Correctly() {
            // Arrange & Act
            Email comAu = new Email("user@domain.com.au");
            Email netAu = new Email("user@domain.net.au");
            Email orgAu = new Email("user@domain.org.au");
            Email govAu = new Email("user@domain.gov.au");

            // Assert
            assertThat(comAu.getDomain()).isEqualTo("domain.com.au");
            assertThat(netAu.getDomain()).isEqualTo("domain.net.au");
            assertThat(orgAu.getDomain()).isEqualTo("domain.org.au");
            assertThat(govAu.getDomain()).isEqualTo("domain.gov.au");
        }

        @Test
        @DisplayName("Should handle email addresses with plus sign tagging")
        void should_HandleEmailAddresses_WithPlusSignTagging() {
            // Arrange
            Email taggedEmail = new Email("user+newsletter@example.com");

            // Act & Assert
            assertThat(taggedEmail.getLocalPart()).isEqualTo("user+newsletter");
            assertThat(taggedEmail.getDomain()).isEqualTo("example.com");
            assertThat(taggedEmail.getValue()).isEqualTo("user+newsletter@example.com");
        }

        @Test
        @DisplayName("Should handle email addresses with dots in local part")
        void should_HandleEmailAddresses_WithDotsInLocalPart() {
            // Arrange
            Email dottedEmail = new Email("first.middle.last@example.com");

            // Act & Assert
            assertThat(dottedEmail.getLocalPart()).isEqualTo("first.middle.last");
            assertThat(dottedEmail.getDomain()).isEqualTo("example.com");
        }

        @Test
        @DisplayName("Should handle subdomain email addresses")
        void should_HandleSubdomainEmail_Addresses() {
            // Arrange
            Email subdomainEmail = new Email("user@mail.company.com.au");

            // Act & Assert
            assertThat(subdomainEmail.getLocalPart()).isEqualTo("user");
            assertThat(subdomainEmail.getDomain()).isEqualTo("mail.company.com.au");
        }

        @Test
        @DisplayName("Should consistently normalize similar emails")
        void should_ConsistentlyNormalize_SimilarEmails() {
            // Arrange
            Email email1 = new Email("Test.User@Example.COM");
            Email email2 = new Email("test.user@example.com");
            Email email3 = new Email("  TEST.USER@EXAMPLE.COM  ");

            // Act & Assert - All should be normalized to the same value
            assertThat(email1.getValue()).isEqualTo("test.user@example.com");
            assertThat(email2.getValue()).isEqualTo("test.user@example.com");
            assertThat(email3.getValue()).isEqualTo("test.user@example.com");
            
            // All should be equal
            assertThat(email1).isEqualTo(email2);
            assertThat(email2).isEqualTo(email3);
            assertThat(email1).isEqualTo(email3);
        }
    }

    @Nested
    @DisplayName("Email Edge Cases")
    class EmailEdgeCasesTests {

        @Test
        @DisplayName("Should handle very long but valid emails")
        void should_HandleVeryLongButValid_Emails() {
            // Arrange - Create a long but valid email
            String longLocalPart = "a".repeat(64); // Max local part length
            String longDomain = "b".repeat(60) + ".com"; // Long but valid domain
            String longEmail = longLocalPart + "@" + longDomain;

            // Act & Assert
            assertThatNoException().isThrownBy(() -> new Email(longEmail));
            Email email = new Email(longEmail);
            assertThat(email.getLocalPart()).hasSize(64);
            assertThat(email.getDomain()).endsWith(".com");
        }

        @Test
        @DisplayName("Should handle numeric domains correctly")
        void should_HandleNumericDomains_Correctly() {
            // Note: IP addresses are not valid in email domains according to our pattern
            // but numeric domain names are valid
            Email numericDomain = new Email("user@123domain.com");
            
            assertThat(numericDomain.getDomain()).isEqualTo("123domain.com");
            assertThat(numericDomain.getLocalPart()).isEqualTo("user");
        }

        @Test
        @DisplayName("Should handle minimum valid email length")
        void should_HandleMinimumValid_EmailLength() {
            // Arrange - Shortest possible valid email: "a@b.co"
            Email minEmail = new Email("a@b.co");

            // Act & Assert
            assertThat(minEmail.getLocalPart()).isEqualTo("a");
            assertThat(minEmail.getDomain()).isEqualTo("b.co");
            assertThat(minEmail.getValue()).isEqualTo("a@b.co");
        }
    }
}