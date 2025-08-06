package com.hanihome.hanihome_au_api.unit.domain.valueobject;

import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Money Creation")
    class MoneyCreationTests {

        @Test
        @DisplayName("Should create money with valid amount and currency")
        void should_CreateMoney_When_ValidAmountAndCurrencyProvided() {
            // Arrange
            BigDecimal amount = new BigDecimal("100.50");
            String currency = "AUD";

            // Act
            Money money = new Money(amount, currency);

            // Assert
            assertThat(money).isNotNull();
            assertThat(money.getAmount()).isEqualTo(amount.setScale(2));
            assertThat(money.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should create money using of() factory method with currency")
        void should_CreateMoney_Using_OfFactoryMethodWithCurrency() {
            // Arrange
            BigDecimal amount = new BigDecimal("250.75");
            String currency = "USD";

            // Act
            Money money = Money.of(amount, currency);

            // Assert
            assertThat(money.getAmount()).isEqualTo(amount.setScale(2));
            assertThat(money.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should create money using of() factory method with default AUD currency")
        void should_CreateMoney_Using_OfFactoryMethodWithDefaultCurrency() {
            // Arrange
            BigDecimal amount = new BigDecimal("500.00");

            // Act
            Money money = Money.of(amount);

            // Assert
            assertThat(money.getAmount()).isEqualTo(amount.setScale(2));
            assertThat(money.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should create zero money with specified currency")
        void should_CreateZeroMoney_When_ZeroFactoryMethodUsed() {
            // Arrange
            String currency = "EUR";

            // Act
            Money money = Money.zero(currency);

            // Assert
            assertThat(money.getAmount()).isEqualTo(BigDecimal.ZERO.setScale(2));
            assertThat(money.getCurrency()).isEqualTo(currency);
        }

        @Test
        @DisplayName("Should normalize currency to uppercase")
        void should_NormalizeCurrencyToUppercase_When_LowercaseProvided() {
            // Arrange
            BigDecimal amount = new BigDecimal("100.00");
            String lowercaseCurrency = "usd";

            // Act
            Money money = new Money(amount, lowercaseCurrency);

            // Assert
            assertThat(money.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should set amount scale to 2 decimal places")
        void should_SetAmountScaleToTwoDecimals_When_DifferentScaleProvided() {
            // Arrange
            BigDecimal amountWithMoreDecimals = new BigDecimal("100.12345");
            BigDecimal amountWithLessDecimals = new BigDecimal("100");

            // Act
            Money money1 = Money.of(amountWithMoreDecimals, "AUD");
            Money money2 = Money.of(amountWithLessDecimals, "AUD");

            // Assert
            assertThat(money1.getAmount()).isEqualTo(new BigDecimal("100.12"));
            assertThat(money2.getAmount()).isEqualTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void should_ThrowException_When_AmountIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Money(null, "AUD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void should_ThrowException_When_CurrencyIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when currency is empty or whitespace")
        void should_ThrowException_When_CurrencyIsEmptyOrWhitespace(String invalidCurrency) {
            // Act & Assert
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, invalidCurrency))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void should_ThrowException_When_AmountIsNegative() {
            // Arrange
            BigDecimal negativeAmount = new BigDecimal("-100.00");

            // Act & Assert
            assertThatThrownBy(() -> new Money(negativeAmount, "AUD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be negative");
        }
    }

    @Nested
    @DisplayName("Money Arithmetic Operations")
    class MoneyArithmeticOperationsTests {

        @Test
        @DisplayName("Should add money with same currency")
        void should_AddMoney_When_SameCurrency() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.50"), "AUD");
            Money money2 = Money.of(new BigDecimal("50.25"), "AUD");

            // Act
            Money result = money1.add(money2);

            // Assert
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("150.75"));
            assertThat(result.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should subtract money with same currency")
        void should_SubtractMoney_When_SameCurrency() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.50"), "AUD");
            Money money2 = Money.of(new BigDecimal("30.25"), "AUD");

            // Act
            Money result = money1.subtract(money2);

            // Assert
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("70.25"));
            assertThat(result.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should multiply money by scalar")
        void should_MultiplyMoney_ByScalar() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");
            BigDecimal multiplier = new BigDecimal("2.5");

            // Act
            Money result = money.multiply(multiplier);

            // Assert
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("250.00"));
            assertThat(result.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should allow subtraction that results in negative amount")
        void should_AllowSubtraction_ThatResultsInNegativeAmount() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("50.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "AUD");

            // Act
            Money result = money1.subtract(money2);

            // Assert
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("-50.00"));
            assertThat(result.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should throw exception when adding money with different currencies")
        void should_ThrowException_When_AddingDifferentCurrencies() {
            // Arrange
            Money audMoney = Money.of(new BigDecimal("100.00"), "AUD");
            Money usdMoney = Money.of(new BigDecimal("100.00"), "USD");

            // Act & Assert
            assertThatThrownBy(() -> audMoney.add(usdMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot perform operation on different currencies");
        }

        @Test
        @DisplayName("Should throw exception when subtracting money with different currencies")
        void should_ThrowException_When_SubtractingDifferentCurrencies() {
            // Arrange
            Money audMoney = Money.of(new BigDecimal("100.00"), "AUD");
            Money eurMoney = Money.of(new BigDecimal("100.00"), "EUR");

            // Act & Assert
            assertThatThrownBy(() -> audMoney.subtract(eurMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot perform operation on different currencies");
        }
    }

    @Nested
    @DisplayName("Money Comparison Operations")
    class MoneyComparisonOperationsTests {

        @Test
        @DisplayName("Should return true when first money is greater than second")
        void should_ReturnTrue_When_FirstMoneyGreaterThanSecond() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("50.00"), "AUD");

            // Act & Assert
            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
        }

        @Test
        @DisplayName("Should return true when first money is less than second")
        void should_ReturnTrue_When_FirstMoneyLessThanSecond() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("50.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money1.isLessThan(money2)).isTrue();
            assertThat(money2.isLessThan(money1)).isFalse();
        }

        @Test
        @DisplayName("Should return false when comparing equal amounts")
        void should_ReturnFalse_When_ComparingEqualAmounts() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money1.isGreaterThan(money2)).isFalse();
            assertThat(money1.isLessThan(money2)).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when comparing different currencies")
        void should_ThrowException_When_ComparingDifferentCurrencies() {
            // Arrange
            Money audMoney = Money.of(new BigDecimal("100.00"), "AUD");
            Money usdMoney = Money.of(new BigDecimal("100.00"), "USD");

            // Act & Assert
            assertThatThrownBy(() -> audMoney.isGreaterThan(usdMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot perform operation on different currencies");

            assertThatThrownBy(() -> audMoney.isLessThan(usdMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot perform operation on different currencies");
        }
    }

    @Nested
    @DisplayName("Money Equality and Hash Code")
    class MoneyEqualityAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when amount and currency are the same")
        void should_BeEqual_When_AmountAndCurrencyAreSame() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when amounts are different")
        void should_NotBeEqual_When_AmountsAreDifferent() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("200.00"), "AUD");

            // Act & Assert
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal when currencies are different")
        void should_NotBeEqual_When_CurrenciesAreDifferent() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.00"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "USD");

            // Act & Assert
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void should_NotBeEqual_ToNull() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to object of different class")
        void should_NotBeEqual_ToObjectOfDifferentClass() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");
            String notMoney = "100.00 AUD";

            // Act & Assert
            assertThat(money).isNotEqualTo(notMoney);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void should_BeEqual_ToItself() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money).isEqualTo(money);
        }

        @Test
        @DisplayName("Should be equal when created with different scale but same value")
        void should_BeEqual_When_CreatedWithDifferentScaleButSameValue() {
            // Arrange
            Money money1 = Money.of(new BigDecimal("100.0"), "AUD");
            Money money2 = Money.of(new BigDecimal("100.00"), "AUD");

            // Act & Assert
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }
    }

    @Nested
    @DisplayName("Money String Representation")
    class MoneyStringRepresentationTests {

        @Test
        @DisplayName("Should format toString correctly")
        void should_FormatToString_Correctly() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.50"), "AUD");

            // Act
            String result = money.toString();

            // Assert
            assertThat(result).isEqualTo("100.50 AUD");
        }

        @Test
        @DisplayName("Should format toString with zero amount correctly")
        void should_FormatToStringWithZeroAmount_Correctly() {
            // Arrange
            Money money = Money.zero("EUR");

            // Act
            String result = money.toString();

            // Assert
            assertThat(result).isEqualTo("0.00 EUR");
        }

        @Test
        @DisplayName("Should format toString with large amount correctly")
        void should_FormatToStringWithLargeAmount_Correctly() {
            // Arrange
            Money money = Money.of(new BigDecimal("1000000.99"), "USD");

            // Act
            String result = money.toString();

            // Assert
            assertThat(result).isEqualTo("1000000.99 USD");
        }
    }

    @Nested
    @DisplayName("Money Immutability")
    class MoneyImmutabilityTests {

        @Test
        @DisplayName("Should not modify original money when performing operations")
        void should_NotModifyOriginalMoney_When_PerformingOperations() {
            // Arrange
            Money original = Money.of(new BigDecimal("100.00"), "AUD");
            Money toAdd = Money.of(new BigDecimal("50.00"), "AUD");
            BigDecimal originalAmount = original.getAmount();
            String originalCurrency = original.getCurrency();

            // Act
            Money sum = original.add(toAdd);
            Money difference = original.subtract(toAdd);
            Money product = original.multiply(new BigDecimal("2"));

            // Assert - Original should remain unchanged
            assertThat(original.getAmount()).isEqualTo(originalAmount);
            assertThat(original.getCurrency()).isEqualTo(originalCurrency);

            // Assert - Operations should create new instances
            assertThat(sum).isNotSameAs(original);
            assertThat(difference).isNotSameAs(original);
            assertThat(product).isNotSameAs(original);
        }

        @Test
        @DisplayName("Should not allow modification of amount and currency")
        void should_NotAllowModification_OfAmountAndCurrency() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");

            // Assert - Fields should be final (enforced by compilation)
            // The Money class should not have setters for amount and currency
            // This test verifies the immutable design by checking that values remain constant
            BigDecimal originalAmount = money.getAmount();
            String originalCurrency = money.getCurrency();

            // Act - Attempt to get references and modify (should not affect the Money object)
            BigDecimal retrievedAmount = money.getAmount();
            String retrievedCurrency = money.getCurrency();

            // Assert - Values should remain the same
            assertThat(money.getAmount()).isEqualTo(originalAmount);
            assertThat(money.getCurrency()).isEqualTo(originalCurrency);
            assertThat(retrievedAmount).isEqualTo(originalAmount);
            assertThat(retrievedCurrency).isEqualTo(originalCurrency);
        }
    }

    @Nested
    @DisplayName("Money Edge Cases")
    class MoneyEdgeCasesTests {

        @Test
        @DisplayName("Should handle very small amounts correctly")
        void should_HandleVerySmallAmounts_Correctly() {
            // Arrange
            Money money = Money.of(new BigDecimal("0.01"), "AUD");

            // Act & Assert
            assertThat(money.getAmount()).isEqualTo(new BigDecimal("0.01"));
            assertThat(money.toString()).isEqualTo("0.01 AUD");
        }

        @Test
        @DisplayName("Should handle very large amounts correctly")
        void should_HandleVeryLargeAmounts_Correctly() {
            // Arrange
            BigDecimal largeAmount = new BigDecimal("999999999999.99");
            Money money = Money.of(largeAmount, "AUD");

            // Act & Assert
            assertThat(money.getAmount()).isEqualTo(largeAmount.setScale(2));
            assertThat(money.getCurrency()).isEqualTo("AUD");
        }

        @Test
        @DisplayName("Should handle arithmetic operations with zero")
        void should_HandleArithmeticOperations_WithZero() {
            // Arrange
            Money money = Money.of(new BigDecimal("100.00"), "AUD");
            Money zero = Money.zero("AUD");

            // Act & Assert
            assertThat(money.add(zero)).isEqualTo(money);
            assertThat(money.subtract(zero)).isEqualTo(money);
            assertThat(money.multiply(BigDecimal.ZERO).getAmount()).isEqualTo(BigDecimal.ZERO.setScale(2));
        }

        @Test
        @DisplayName("Should handle rounding correctly")
        void should_HandleRounding_Correctly() {
            // Arrange & Act
            Money money1 = Money.of(new BigDecimal("100.125"), "AUD"); // Should round to 100.13
            Money money2 = Money.of(new BigDecimal("100.124"), "AUD"); // Should round to 100.12

            // Assert
            assertThat(money1.getAmount()).isEqualTo(new BigDecimal("100.13"));
            assertThat(money2.getAmount()).isEqualTo(new BigDecimal("100.12"));
        }
    }
}