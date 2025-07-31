package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, PropertyRepositoryCustom {

    // Basic status-based queries
    List<Property> findByStatus(PropertyStatus status);
    
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    // Landlord-specific queries
    List<Property> findByLandlordId(Long landlordId);
    
    Page<Property> findByLandlordId(Long landlordId, Pageable pageable);
    
    Page<Property> findByLandlordIdAndStatus(Long landlordId, PropertyStatus status, Pageable pageable);

    // Agent-specific queries
    List<Property> findByAgentId(Long agentId);
    
    Page<Property> findByAgentId(Long agentId, Pageable pageable);

    // Property type and rental type queries
    Page<Property> findByPropertyTypeAndStatus(PropertyType propertyType, PropertyStatus status, Pageable pageable);
    
    Page<Property> findByRentalTypeAndStatus(RentalType rentalType, PropertyStatus status, Pageable pageable);

    // Price range queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND (:minDeposit IS NULL OR p.deposit >= :minDeposit) " +
           "AND (:maxDeposit IS NULL OR p.deposit <= :maxDeposit) " +
           "AND (:minRent IS NULL OR p.monthlyRent >= :minRent) " +
           "AND (:maxRent IS NULL OR p.monthlyRent <= :maxRent)")
    Page<Property> findByStatusAndPriceRange(
        @Param("status") PropertyStatus status,
        @Param("minDeposit") BigDecimal minDeposit,
        @Param("maxDeposit") BigDecimal maxDeposit,
        @Param("minRent") BigDecimal minRent,
        @Param("maxRent") BigDecimal maxRent,
        Pageable pageable
    );

    // Area-based queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND (:minArea IS NULL OR p.area >= :minArea) " +
           "AND (:maxArea IS NULL OR p.area <= :maxArea)")
    Page<Property> findByStatusAndArea(
        @Param("status") PropertyStatus status,
        @Param("minArea") BigDecimal minArea,
        @Param("maxArea") BigDecimal maxArea,
        Pageable pageable
    );

    // Location-based queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND p.city LIKE %:city%")
    Page<Property> findByStatusAndCity(
        @Param("status") PropertyStatus status,
        @Param("city") String city,
        Pageable pageable
    );

    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND p.district LIKE %:district%")
    Page<Property> findByStatusAndDistrict(
        @Param("status") PropertyStatus status,
        @Param("district") String district,
        Pageable pageable
    );

    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND p.address LIKE %:keyword%")
    Page<Property> findByStatusAndAddressContaining(
        @Param("status") PropertyStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Geolocation queries (within radius)
    @Query(value = "SELECT * FROM properties p WHERE p.status = :status " +
                   "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
                   "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
                   "cos(radians(p.longitude) - radians(:longitude)) + " +
                   "sin(radians(:latitude)) * sin(radians(p.latitude)))) <= :radiusKm",
           nativeQuery = true)
    List<Property> findByStatusAndLocationWithinRadius(
        @Param("status") String status,
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("radiusKm") double radiusKm
    );

    // Room-based queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND (:minRooms IS NULL OR p.rooms >= :minRooms) " +
           "AND (:maxRooms IS NULL OR p.rooms <= :maxRooms)")
    Page<Property> findByStatusAndRoomRange(
        @Param("status") PropertyStatus status,
        @Param("minRooms") Integer minRooms,
        @Param("maxRooms") Integer maxRooms,
        Pageable pageable
    );

    // Options-based queries
    @Query("SELECT DISTINCT p FROM Property p JOIN p.options opt " +
           "WHERE p.status = :status AND opt IN :options")
    Page<Property> findByStatusAndOptionsIn(
        @Param("status") PropertyStatus status,
        @Param("options") List<String> options,
        Pageable pageable
    );

    // Facilities queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND (:parkingRequired IS NULL OR p.parkingAvailable = :parkingRequired) " +
           "AND (:petAllowed IS NULL OR p.petAllowed = :petAllowed) " +
           "AND (:furnished IS NULL OR p.furnished = :furnished)")
    Page<Property> findByStatusAndFacilities(
        @Param("status") PropertyStatus status,
        @Param("parkingRequired") Boolean parkingRequired,
        @Param("petAllowed") Boolean petAllowed,
        @Param("furnished") Boolean furnished,
        Pageable pageable
    );

    // Available date queries
    @Query("SELECT p FROM Property p WHERE p.status = :status " +
           "AND (:availableFrom IS NULL OR p.availableDate >= :availableFrom) " +
           "AND (:availableTo IS NULL OR p.availableDate <= :availableTo)")
    Page<Property> findByStatusAndAvailableDateRange(
        @Param("status") PropertyStatus status,
        @Param("availableFrom") LocalDate availableFrom,
        @Param("availableTo") LocalDate availableTo,
        Pageable pageable
    );

    // Complex search query combining multiple criteria
    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN p.options opt " +
           "WHERE p.status = :status " +
           "AND (:propertyType IS NULL OR p.propertyType = :propertyType) " +
           "AND (:rentalType IS NULL OR p.rentalType = :rentalType) " +
           "AND (:minDeposit IS NULL OR p.deposit >= :minDeposit) " +
           "AND (:maxDeposit IS NULL OR p.deposit <= :maxDeposit) " +
           "AND (:minRent IS NULL OR p.monthlyRent >= :minRent) " +
           "AND (:maxRent IS NULL OR p.monthlyRent <= :maxRent) " +
           "AND (:minArea IS NULL OR p.area >= :minArea) " +
           "AND (:maxArea IS NULL OR p.area <= :maxArea) " +
           "AND (:minRooms IS NULL OR p.rooms >= :minRooms) " +
           "AND (:maxRooms IS NULL OR p.rooms <= :maxRooms) " +
           "AND (:city IS NULL OR p.city LIKE %:city%) " +
           "AND (:district IS NULL OR p.district LIKE %:district%) " +
           "AND (:parking IS NULL OR p.parkingAvailable = :parking) " +
           "AND (:pet IS NULL OR p.petAllowed = :pet) " +
           "AND (:furnished IS NULL OR p.furnished = :furnished) " +
           "AND (:availableFrom IS NULL OR p.availableDate >= :availableFrom) " +
           "AND (:options IS NULL OR opt IN :options)")
    Page<Property> searchProperties(
        @Param("status") PropertyStatus status,
        @Param("propertyType") PropertyType propertyType,
        @Param("rentalType") RentalType rentalType,
        @Param("minDeposit") BigDecimal minDeposit,
        @Param("maxDeposit") BigDecimal maxDeposit,
        @Param("minRent") BigDecimal minRent,
        @Param("maxRent") BigDecimal maxRent,
        @Param("minArea") BigDecimal minArea,
        @Param("maxArea") BigDecimal maxArea,
        @Param("minRooms") Integer minRooms,
        @Param("maxRooms") Integer maxRooms,
        @Param("city") String city,
        @Param("district") String district,
        @Param("parking") Boolean parking,
        @Param("pet") Boolean pet,
        @Param("furnished") Boolean furnished,
        @Param("availableFrom") LocalDate availableFrom,
        @Param("options") List<String> options,
        Pageable pageable
    );

    // Statistics queries
    @Query("SELECT COUNT(p) FROM Property p WHERE p.status = :status")
    long countByStatus(@Param("status") PropertyStatus status);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.landlordId = :landlordId AND p.status = :status")
    long countByLandlordIdAndStatus(@Param("landlordId") Long landlordId, @Param("status") PropertyStatus status);

    @Query("SELECT p.propertyType, COUNT(p) FROM Property p WHERE p.status = :status GROUP BY p.propertyType")
    List<Object[]> countByStatusGroupByPropertyType(@Param("status") PropertyStatus status);

    @Query("SELECT p.city, COUNT(p) FROM Property p WHERE p.status = :status GROUP BY p.city ORDER BY COUNT(p) DESC")
    List<Object[]> countByStatusGroupByCity(@Param("status") PropertyStatus status);

    // Recent properties
    @Query("SELECT p FROM Property p WHERE p.status = :status ORDER BY p.createdDate DESC")
    Page<Property> findRecentByStatus(@Param("status") PropertyStatus status, Pageable pageable);

    // Properties pending approval for agents
    @Query("SELECT p FROM Property p WHERE p.status = 'PENDING_APPROVAL' ORDER BY p.createdDate ASC")
    Page<Property> findPropertiesPendingApproval(Pageable pageable);

    // Check if property belongs to landlord
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Property p " +
           "WHERE p.id = :propertyId AND p.landlordId = :landlordId")
    boolean existsByIdAndLandlordId(@Param("propertyId") Long propertyId, @Param("landlordId") Long landlordId);

    // Get property with owner validation
    @Query("SELECT p FROM Property p WHERE p.id = :propertyId AND p.landlordId = :landlordId")
    Optional<Property> findByIdAndLandlordId(@Param("propertyId") Long propertyId, @Param("landlordId") Long landlordId);
}