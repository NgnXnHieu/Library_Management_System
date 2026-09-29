package com.library.mapper;

import com.library.dto.account.AccountResponseDto;
import com.library.entity.Account;
import com.library.requestform.account.RegisterRequestForm;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface AccountMapper {

    /**
     * Map từ RegisterRequestForm sang Account entity
     * (Mật khẩu passwordHash sẽ được mã hóa BCrypt riêng tại Service)
     */
    @Mapping(target = "status", constant = "ACTIVE")
    Account toEntity(RegisterRequestForm form);

    /**
     * Map từ Entity Account sang AccountResponseDto (an toàn, không lộ mật khẩu)
     */
    AccountResponseDto toDto(Account account);

    /**
     * Map danh sách Entity Account sang danh sách DTO
     */
    List<AccountResponseDto> toDtoList(List<Account> accounts);
}
