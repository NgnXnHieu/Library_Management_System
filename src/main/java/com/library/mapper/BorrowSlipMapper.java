package com.library.mapper;

import com.library.dto.borrow.BorrowItemResponseDto;
import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.entity.BorrowItem;
import com.library.entity.BorrowSlip;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Entity BorrowSlip, BorrowItem và các DTO tương ứng bằng MapStruct.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface BorrowSlipMapper {

    /**
     * Map từ Entity BorrowSlip sang DTO BorrowSlipResponseDto.
     */
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.fullName", target = "customerName")
    @Mapping(source = "customer.phone", target = "customerPhone")
    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    @Mapping(source = "staff.phone", target = "staffPhone")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    @Mapping(source = "borrowItems", target = "items")
    BorrowSlipResponseDto toDto(BorrowSlip borrowSlip);

    /**
     * Map từ Entity BorrowItem sang DTO BorrowItemResponseDto.
     */
    @Mapping(source = "inventory.id", target = "inventoryId")
    @Mapping(source = "inventory.book.id", target = "bookId")
    @Mapping(source = "inventory.book.title", target = "bookTitle")
    @Mapping(source = "inventory.book.isbn", target = "isbn")
    @Mapping(source = "rentalPrice", target = "rentalPrice")
    BorrowItemResponseDto toBorrowItemDto(BorrowItem item);

    /**
     * Map danh sách Entity BorrowSlip sang danh sách DTO.
     */
    List<BorrowSlipResponseDto> toDtoList(List<BorrowSlip> borrowSlips);
}
