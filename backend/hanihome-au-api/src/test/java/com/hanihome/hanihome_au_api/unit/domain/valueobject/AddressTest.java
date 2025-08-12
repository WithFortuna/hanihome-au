package com.hanihome.hanihome_au_api.unit.domain.valueobject;

import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Value Object Tests")
class AddressTest {

    @Nested
    @DisplayName("Address Creation")
    class AddressCreationTests {

        @Test
        @DisplayName("Should create address with all fields")
        void should_CreateAddress_When_AllFieldsProvided() {
            // Arrange
            String street = "123 Collins Street";
            String city = "Melbourne";
            String state = "VIC";
            String country = "Australia";
            String postalCode = "3000";
            Double latitude = -37.8136;
            Double longitude = 144.9631;

            // Act
            Address address = new Address(street, city, state, country, postalCode, latitude, longitude);

            // Assert
            assertThat(address).isNotNull();
            assertThat(address.getStreet()).isEqualTo(street);
            assertThat(address.getCity()).isEqualTo(city);
            assertThat(address.getState()).isEqualTo(state);
            assertThat(address.getCountry()).isEqualTo(country);
            assertThat(address.getPostalCode()).isEqualTo(postalCode);
            assertThat(address.getLatitude()).isEqualTo(latitude);
            assertThat(address.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should create address with minimal required fields")
        void should_CreateAddress_When_MinimalFieldsProvided() {
            // Arrange
            String city = "Sydney";
            String country = "Australia";

            // Act
            Address address = new Address(null, city, null, country, null, null, null);

            // Assert
            assertThat(address.getCity()).isEqualTo(city);
            assertThat(address.getCountry()).isEqualTo(country);
            assertThat(address.getStreet()).isNull();
            assertThat(address.getState()).isNull();
            assertThat(address.getPostalCode()).isNull();
            assertThat(address.getLatitude()).isNull();
            assertThat(address.getLongitude()).isNull();
        }

        @Test
        @DisplayName("Should trim whitespace from city and country")
        void should_TrimWhitespace_FromCityAndCountry() {
            // Arrange
            String cityWithWhitespace = "  Melbourne  ";
            String countryWithWhitespace = "  Australia  ";

            // Act
            Address address = new Address(null, cityWithWhitespace, null, countryWithWhitespace, null, null, null);

            // Assert
            assertThat(address.getCity()).isEqualTo("Melbourne");
            assertThat(address.getCountry()).isEqualTo("Australia");
        }

        @Test
        @DisplayName("Should throw exception when city is null")
        void should_ThrowException_When_CityIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Address(null, null, null, "Australia", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("City cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when city is empty or whitespace")
        void should_ThrowException_When_CityIsEmptyOrWhitespace(String invalidCity) {
            // Act & Assert
            assertThatThrownBy(() -> new Address(null, invalidCity, null, "Australia", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("City cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when country is null")
        void should_ThrowException_When_CountryIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Address(null, "Sydney", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Country cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when country is empty or whitespace")
        void should_ThrowException_When_CountryIsEmptyOrWhitespace(String invalidCountry) {
            // Act & Assert
            assertThatThrownBy(() -> new Address(null, "Sydney", null, invalidCountry, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Country cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("Address Full Address Generation")
    class AddressFullAddressGenerationTests {

        @Test
        @DisplayName("Should generate full address with all fields")
        void should_GenerateFullAddress_When_AllFieldsProvided() {
            // Arrange
            Address address = new Address(
                "123 Collins Street", 
                "Melbourne", 
                "VIC", 
                "Australia", 
                "3000", 
                -37.8136, 
                144.9631
            );

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("123 Collins Street, Melbourne, VIC 3000, Australia");
        }

        @Test
        @DisplayName("Should generate full address without street")
        void should_GenerateFullAddress_When_StreetIsNull() {
            // Arrange
            Address address = new Address(null, "Melbourne", "VIC", "Australia", "3000", null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("Melbourne, VIC 3000, Australia");
        }

        @Test
        @DisplayName("Should generate full address without state")
        void should_GenerateFullAddress_When_StateIsNull() {
            // Arrange
            Address address = new Address("123 Collins Street", "Melbourne", null, "Australia", "3000", null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("123 Collins Street, Melbourne 3000, Australia");
        }

        @Test
        @DisplayName("Should generate full address without postal code")
        void should_GenerateFullAddress_When_PostalCodeIsNull() {
            // Arrange
            Address address = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", null, null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("123 Collins Street, Melbourne, VIC, Australia");
        }

        @Test
        @DisplayName("Should generate full address with minimal fields")
        void should_GenerateFullAddress_When_OnlyRequiredFieldsProvided() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("Sydney, Australia");
        }

        @Test
        @DisplayName("Should handle empty string fields correctly")
        void should_HandleEmptyStringFields_Correctly() {
            // Arrange
            Address address = new Address("", "Melbourne", "", "Australia", "", null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert - Empty strings should be treated as null/absent
            assertThat(fullAddress).isEqualTo("Melbourne, Australia");
        }

        @Test
        @DisplayName("Should handle whitespace-only fields correctly")
        void should_HandleWhitespaceOnlyFields_Correctly() {
            // Arrange
            Address address = new Address("  ", "Melbourne", "  ", "Australia", "  ", null, null);

            // Act
            String fullAddress = address.getFullAddress();

            // Assert - Whitespace-only strings should be treated as empty
            assertThat(fullAddress).isEqualTo("Melbourne, Australia");
        }
    }

    @Nested
    @DisplayName("Address Coordinates")
    class AddressCoordinatesTests {

        @Test
        @DisplayName("Should have coordinates when both latitude and longitude are provided")
        void should_HaveCoordinates_When_BothLatitudeAndLongitudeProvided() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, -33.8688, 151.2093);

            // Act & Assert
            assertThat(address.hasCoordinates()).isTrue();
        }

        @Test
        @DisplayName("Should not have coordinates when latitude is missing")
        void should_NotHaveCoordinates_When_LatitudeIsMissing() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, 151.2093);

            // Act & Assert
            assertThat(address.hasCoordinates()).isFalse();
        }

        @Test
        @DisplayName("Should not have coordinates when longitude is missing")
        void should_NotHaveCoordinates_When_LongitudeIsMissing() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, -33.8688, null);

            // Act & Assert
            assertThat(address.hasCoordinates()).isFalse();
        }

        @Test
        @DisplayName("Should not have coordinates when both are missing")
        void should_NotHaveCoordinates_When_BothAreMissing() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, null);

            // Act & Assert
            assertThat(address.hasCoordinates()).isFalse();
        }

        @Test
        @DisplayName("Should handle valid coordinate ranges")
        void should_HandleValidCoordinateRanges() {
            // Arrange - Valid Australian coordinates
            Double validLatitude = -25.2744; // Alice Springs
            Double validLongitude = 133.7751;

            // Act
            Address address = new Address(null, "Alice Springs", "NT", "Australia", null, validLatitude, validLongitude);

            // Assert
            assertThat(address.hasCoordinates()).isTrue();
            assertThat(address.getLatitude()).isEqualTo(validLatitude);
            assertThat(address.getLongitude()).isEqualTo(validLongitude);
        }

        @Test
        @DisplayName("Should handle edge coordinate values")
        void should_HandleEdgeCoordinateValues() {
            // Arrange - Edge values (max/min for lat/long)
            Double maxLatitude = 90.0;
            Double minLatitude = -90.0;
            Double maxLongitude = 180.0;
            Double minLongitude = -180.0;

            // Act & Assert
            Address maxAddress = new Address(null, "North Pole", null, "Arctic", null, maxLatitude, maxLongitude);
            Address minAddress = new Address(null, "South Pole", null, "Antarctica", null, minLatitude, minLongitude);

            assertThat(maxAddress.hasCoordinates()).isTrue();
            assertThat(minAddress.hasCoordinates()).isTrue();
        }
    }

    @Nested
    @DisplayName("Address Equality and Hash Code")
    class AddressEqualityAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void should_BeEqual_When_AllFieldsAreSame() {
            // Arrange
            Address address1 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);
            Address address2 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);

            // Act & Assert
            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when streets are different")
        void should_NotBeEqual_When_StreetsAreDifferent() {
            // Arrange
            Address address1 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", null, null);
            Address address2 = new Address("456 Collins Street", "Melbourne", "VIC", "Australia", "3000", null, null);

            // Act & Assert
            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Should not be equal when cities are different")
        void should_NotBeEqual_When_CitiesAreDifferent() {
            // Arrange
            Address address1 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", null, null);
            Address address2 = new Address("123 Collins Street", "Sydney", "VIC", "Australia", "3000", null, null);

            // Act & Assert
            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Should not be equal when coordinates are different")
        void should_NotBeEqual_When_CoordinatesAreDifferent() {
            // Arrange
            Address address1 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);
            Address address2 = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -33.8688, 151.2093);

            // Act & Assert
            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Should be equal when both have null optional fields")
        void should_BeEqual_When_BothHaveNullOptionalFields() {
            // Arrange
            Address address1 = new Address(null, "Sydney", null, "Australia", null, null, null);
            Address address2 = new Address(null, "Sydney", null, "Australia", null, null, null);

            // Act & Assert
            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void should_NotBeEqual_ToNull() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, null);

            // Act & Assert
            assertThat(address).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to object of different class")
        void should_NotBeEqual_ToObjectOfDifferentClass() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, null);
            String notAddress = "Sydney, Australia";

            // Act & Assert
            assertThat(address).isNotEqualTo(notAddress);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void should_BeEqual_ToItself() {
            // Arrange
            Address address = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);

            // Act & Assert
            assertThat(address).isEqualTo(address);
        }
    }

    @Nested
    @DisplayName("Address String Representation")
    class AddressStringRepresentationTests {

        @Test
        @DisplayName("Should return full address as toString")
        void should_ReturnFullAddress_AsToString() {
            // Arrange
            Address address = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);

            // Act
            String result = address.toString();

            // Assert
            assertThat(result).isEqualTo("123 Collins Street, Melbourne, VIC 3000, Australia");
        }

        @Test
        @DisplayName("Should return correct string for minimal address")
        void should_ReturnCorrectString_ForMinimalAddress() {
            // Arrange
            Address address = new Address(null, "Sydney", null, "Australia", null, null, null);

            // Act
            String result = address.toString();

            // Assert
            assertThat(result).isEqualTo("Sydney, Australia");
        }
    }

    @Nested
    @DisplayName("Address Immutability")
    class AddressImmutabilityTests {

        @Test
        @DisplayName("Should not allow modification of fields after creation")
        void should_NotAllowModification_OfFieldsAfterCreation() {
            // Arrange
            String originalStreet = "123 Collins Street";
            String originalCity = "Melbourne";
            String originalState = "VIC";
            String originalCountry = "Australia";
            String originalPostalCode = "3000";
            Double originalLatitude = -37.8136;
            Double originalLongitude = 144.9631;

            Address address = new Address(
                originalStreet, originalCity, originalState, originalCountry, 
                originalPostalCode, originalLatitude, originalLongitude
            );

            // Assert - Fields should remain constant (enforced by final modifier)
            assertThat(address.getStreet()).isEqualTo(originalStreet);
            assertThat(address.getCity()).isEqualTo(originalCity);
            assertThat(address.getState()).isEqualTo(originalState);
            assertThat(address.getCountry()).isEqualTo(originalCountry);
            assertThat(address.getPostalCode()).isEqualTo(originalPostalCode);
            assertThat(address.getLatitude()).isEqualTo(originalLatitude);
            assertThat(address.getLongitude()).isEqualTo(originalLongitude);

            // Multiple calls should return the same values
            assertThat(address.getStreet()).isEqualTo(originalStreet);
            assertThat(address.getCity()).isEqualTo(originalCity);
        }

        @Test
        @DisplayName("Should maintain reference equality for immutable string fields")
        void should_MaintainReferenceEquality_ForImmutableStringFields() {
            // Arrange
            Address address = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);

            // Act - Get references multiple times
            String street1 = address.getStreet();
            String street2 = address.getStreet();
            String city1 = address.getCity();
            String city2 = address.getCity();

            // Assert - Should return the same references
            assertThat(street1).isSameAs(street2);
            assertThat(city1).isSameAs(city2);
        }
    }

    @Nested
    @DisplayName("Address Business Logic")
    class AddressBusinessLogicTests {

        @Test
        @DisplayName("Should be suitable for Australian addresses")
        void should_BeSuitable_ForAustralianAddresses() {
            // Arrange & Act
            Address sydneyAddress = new Address("1 Bridge Street", "Sydney", "NSW", "Australia", "2000", -33.8688, 151.2093);
            Address melbourneAddress = new Address("123 Collins Street", "Melbourne", "VIC", "Australia", "3000", -37.8136, 144.9631);
            Address brisbaneAddress = new Address("456 Queen Street", "Brisbane", "QLD", "Australia", "4000", -27.4698, 153.0251);

            // Assert
            assertThat(sydneyAddress.getFullAddress()).contains("NSW", "Australia");
            assertThat(melbourneAddress.getFullAddress()).contains("VIC", "Australia");
            assertThat(brisbaneAddress.getFullAddress()).contains("QLD", "Australia");
            assertThat(sydneyAddress.hasCoordinates()).isTrue();
            assertThat(melbourneAddress.hasCoordinates()).isTrue();
            assertThat(brisbaneAddress.hasCoordinates()).isTrue();
        }

        @Test
        @DisplayName("Should handle international addresses correctly")
        void should_HandleInternationalAddresses_Correctly() {
            // Arrange & Act
            Address usAddress = new Address("123 Main Street", "New York", "NY", "United States", "10001", 40.7589, -73.9851);
            Address ukAddress = new Address("10 Downing Street", "London", null, "United Kingdom", "SW1A 2AA", 51.5074, -0.1278);

            // Assert
            assertThat(usAddress.getFullAddress()).isEqualTo("123 Main Street, New York, NY 10001, United States");
            assertThat(ukAddress.getFullAddress()).isEqualTo("10 Downing Street, London SW1A 2AA, United Kingdom");
        }

        @Test
        @DisplayName("Should validate that required fields are properly enforced")
        void should_ValidateThatRequiredFields_AreProperlyEnforced() {
            // The validation happens in the constructor, so this test verifies
            // that the business rules are properly implemented

            // Valid addresses should work
            assertThatNoException().isThrownBy(() -> 
                new Address(null, "Sydney", null, "Australia", null, null, null)
            );

            // Invalid addresses should fail
            assertThatThrownBy(() -> 
                new Address(null, null, null, "Australia", null, null, null)
            ).isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> 
                new Address(null, "Sydney", null, null, null, null, null)
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }
}