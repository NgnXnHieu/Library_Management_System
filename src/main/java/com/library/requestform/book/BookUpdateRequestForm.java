package com.library.requestform.book;

import com.library.enums.DisplayStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request form cập nhật thông tin đầu sách.
 * Tất cả các trường đều là tùy chọn (optional).
 * Các trường không truyền (mang giá trị null) sẽ được giữ nguyên dữ liệu hiện
 * tại trong Database.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookUpdateRequestForm {

    private Long categoryId;

    @Size(max = 20, message = "Mã ISBN tối đa 20 ký tự")
    private String isbn;

    @Size(max = 255, message = "Tiêu đề sách tối đa 255 ký tự")
    private String title;

    @Size(max = 150, message = "Tên tác giả tối đa 150 ký tự")
    private String author;

    @Size(max = 150, message = "Nhà xuất bản tối đa 150 ký tự")
    private String publisher;

    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    private Integer publicationYear;

    private String description;

    @Size(max = 255, message = "Khóa ảnh bìa tối đa 255 ký tự")
    private String coverImageKey;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sách phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá mượn không được âm")
    private BigDecimal rentalPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tiền phạt không được âm")
    private BigDecimal fineAmount;

    private DisplayStatus status;
}
